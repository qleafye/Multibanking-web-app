package com.studfi.backend.client.vbank;

import com.studfi.backend.client.BankClient;
import com.studfi.backend.client.vbank.dto.account.VBankAccountDto;
import com.studfi.backend.client.vbank.dto.account.VBankAccountListResponseDto;
import com.studfi.backend.client.vbank.dto.auth.VBankAuthResponse;
import com.studfi.backend.client.vbank.dto.consent.VBankConsentRequest;
import com.studfi.backend.client.vbank.dto.consent.VBankConsentResponse;
import com.studfi.backend.client.vbank.dto.transaction.VBankTransactionDto;
import com.studfi.backend.client.vbank.dto.transaction.VBankTransactionListResponseDto;
import com.studfi.backend.dto.Account;
import com.studfi.backend.dto.Transaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class VBankApiClient implements BankClient {

    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${vbank.api.baseurl}") private String baseUrl;
    @Value("${vbank.api.client-id}") private String teamClientId;
    @Value("${vbank.api.client-secret}") private String clientSecret;

    @Override
    public String getBankName() { return "VBank"; }

    @Override
    public List<Transaction> getAllTransactionsForClient(String endUserClientId) {
        String token = getAuthToken();
        if (token == null) return Collections.emptyList();

        String consentId = createConsent(token, endUserClientId);
        if (consentId == null) return Collections.emptyList();

        List<Account> accounts = getAccounts(token, consentId, endUserClientId);
        if (accounts.isEmpty()) {
            System.out.println("VBank: Счетов для клиента " + endUserClientId + " не найдено. Транзакции не запрашиваем.");
            return Collections.emptyList();
        }

        return accounts.stream()
                .flatMap(account -> getTransactions(token, consentId, endUserClientId, account.getAccountId()).stream())
                .collect(Collectors.toList());
    }

    private String getAuthToken() {
        // ... (без изменений)
        String authUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/auth/bank-token").queryParam("client_id", teamClientId).queryParam("client_secret", clientSecret).toUriString();
        try {
            ResponseEntity<VBankAuthResponse> response = restTemplate.postForEntity(authUrl, null, VBankAuthResponse.class);
            System.out.println("VBank: Токен успешно получен!");
            return response.getBody().getAccessToken();
        } catch (Exception e) { System.err.println("VBank: ОШИБКА АУТЕНТИФИКАЦИИ! " + e.getMessage()); return null; }
    }

    private String createConsent(String token, String endUserClientId) {
        // ... (без изменений)
        String consentUrl = baseUrl + "/account-consents/request";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        headers.set("X-Requesting-Bank", teamClientId);
        VBankConsentRequest requestBody = new VBankConsentRequest(endUserClientId, teamClientId);
        HttpEntity<VBankConsentRequest> requestEntity = new HttpEntity<>(requestBody, headers);
        try {
            ResponseEntity<VBankConsentResponse> response = restTemplate.postForEntity(consentUrl, requestEntity, VBankConsentResponse.class);
            String consentId = response.getBody().getConsentId();
            System.out.println("VBank: Согласие для " + endUserClientId + " успешно создано! ID: " + consentId);
            return consentId;
        } catch (Exception e) { System.err.println("VBank: ОШИБКА СОЗДАНИЯ СОГЛАСИЯ для " + endUserClientId + "! " + e.getMessage()); return null; }
    }

    private List<Account> getAccounts(String token, String consentId, String endUserClientId) {
        // ... (этот метод уже работает, не меняем)
        String accountsUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/accounts").queryParam("client_id", endUserClientId).toUriString();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.set("X-Requesting-Bank", teamClientId);
        headers.set("X-Consent-Id", consentId);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        try {
            ResponseEntity<VBankAccountListResponseDto> response = restTemplate.exchange(accountsUrl, HttpMethod.GET, requestEntity, VBankAccountListResponseDto.class);
            List<VBankAccountDto> bankAccounts = response.getBody().getData().getAccount();
            if (bankAccounts == null) return Collections.emptyList();
            System.out.println("VBank: Успешно получено счетов: " + bankAccounts.size());
            return bankAccounts.stream().map(bankAcc -> new Account(bankAcc.getAccountId(), getBankName())).collect(Collectors.toList());
        } catch (Exception e) { System.err.println("VBank: ОШИБКА ПОЛУЧЕНИЯ СЧЕТОВ для " + endUserClientId + "! " + e.getMessage()); return Collections.emptyList(); }
    }

    private List<Transaction> getTransactions(String token, String consentId, String endUserClientId, String accountId) {
        String transactionsUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/accounts/" + accountId + "/transactions")
                .queryParam("client_id", endUserClientId).toUriString();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.set("X-Requesting-Bank", teamClientId);
        headers.set("X-Consent-Id", consentId);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            // Предполагаем, что у транзакций тоже сложная структура
            ResponseEntity<VBankTransactionListResponseDto> response = restTemplate.exchange(
                    transactionsUrl,
                    HttpMethod.GET,
                    requestEntity,
                    VBankTransactionListResponseDto.class
            );
            List<VBankTransactionDto> bankTransactions = response.getBody().getData().getTransaction();

            if (bankTransactions == null) return Collections.emptyList();
            System.out.println("VBank: Для счета " + accountId + " получено транзакций: " + bankTransactions.size());

            // --- ИСПРАВЛЕННАЯ ЛОГИКА КОНВЕРТАЦИИ ---
            return bankTransactions.stream()
                    .map(bankDto -> new Transaction(
                            bankDto.getTransactionInformation(), // Используем новое поле
                            Double.parseDouble(bankDto.getAmount().getAmount()), // Достаем сумму из вложенного объекта и преобразуем в число
                            bankDto.getBookingDateTime() // Используем новое поле
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("VBank: ОШИБКА ПОЛУЧЕНИЯ ТРАНЗАКЦИЙ для счета " + accountId + "! " + e.getMessage());
            return Collections.emptyList();
        }
    }
}