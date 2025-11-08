package com.client.vbank;

import com.client.BankClient;
import com.client.vbank.dto.account.VBankAccountDto;
import com.client.vbank.dto.account.VBankAccountListResponseDto;
import com.client.vbank.dto.account.VBankBalanceResponseDto;
import com.client.vbank.dto.consent.VBankConsentDetailsDto;
import com.client.vbank.dto.consent.VBankConsentRequest;
import com.client.vbank.dto.consent.VBankConsentResponse;
import com.client.vbank.dto.transaction.VBankTransactionDto;
import com.client.vbank.dto.transaction.VBankTransactionListResponseDto;
import com.client.vbank.dto.account.*;
import com.client.vbank.dto.auth.VBankAuthResponse;
import com.client.vbank.dto.consent.*;
import com.client.vbank.dto.product.VBankProductDto;
import com.client.vbank.dto.product.VBankProductListResponseDto;
import com.client.vbank.dto.transaction.*;
import com.dto.Account;
import com.dto.Product; // <-- ВОТ ЧЕГО НЕ ХВАТАЛО
import com.dto.Transaction;
import com.service.ConsentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class VBankApiClient implements BankClient {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired private ConsentService consentService;
    @Value("${vbank.api.baseurl}") private String baseUrl;
    @Value("${vbank.api.client-id}") private String teamClientId;
    @Value("${vbank.api.client-secret}") private String clientSecret;

    @Override
    public String getBankName() { return "VBank"; }

    @Override
    public List<Transaction> getAllTransactionsForClient(String endUserClientId) {
        String token = getAuthToken();
        if (token == null) return Collections.emptyList();
        String consentId = getOrCreateConsent(token, endUserClientId);
        if (consentId == null) return Collections.emptyList();
        List<Account> accounts = getEnrichedAccounts(token, consentId, endUserClientId);
        if (accounts.isEmpty()) return Collections.emptyList();

        final String finalConsentId = consentId; // Создаем effectively final переменную
        return accounts.stream()
                .flatMap(account -> getTransactions(token, finalConsentId, endUserClientId, account.getAccountId()).stream())
                .collect(Collectors.toList());
    }

    public List<Account> getEnrichedAccountsForClient(String endUserClientId) {
        String token = getAuthToken();
        if (token == null) return Collections.emptyList();
        String consentId = getOrCreateConsent(token, endUserClientId);
        if (consentId == null) return Collections.emptyList();
        return getEnrichedAccounts(token, consentId, endUserClientId);
    }


    @Override
    public List<Product> getAvailableProducts() {
        String productsUrl = baseUrl + "/products";
        System.out.println("VBank: Запрашиваем каталог продуктов...");
        try {
            // Ожидаем получить "внешнюю коробку"
            VBankProductListResponseDto response = restTemplate.getForObject(productsUrl, VBankProductListResponseDto.class);

            // "Распаковываем" ее: response -> data -> product
            List<VBankProductDto> bankProducts = response.getData().getProduct();

            if (bankProducts == null) return Collections.emptyList();
            System.out.println("VBank: Успешно получено продуктов: " + bankProducts.size());
            return bankProducts.stream().map(bankProduct -> {
                Product product = new Product();
                product.setProductId(bankProduct.getProductId());
                product.setProductName(bankProduct.getProductName());
                product.setProductType(bankProduct.getProductType());
                product.setDescription(bankProduct.getDescription());
                return product;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("VBank: ОШИБКА ПОЛУЧЕНИЯ КАТАЛОГА ПРОДУКТОВ! " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public VBankConsentDetailsDto getConsentDetails(String consentId) {
        String consentUrl = baseUrl + "/account-consents/" + consentId;
        try {
            return restTemplate.getForObject(consentUrl, VBankConsentDetailsDto.class);
        } catch (Exception e) { System.err.println("VBank: ОШИБКА ПОЛУЧЕНИЯ ДЕТАЛЕЙ СОГЛАСИЯ! " + e.getMessage()); return null; }
    }

    public void revokeConsent(String endUserClientId) {
        String consentId = consentService.getConsent(getBankName(), endUserClientId);
        if (consentId == null) return;
        String consentUrl = baseUrl + "/account-consents/" + consentId;
        try {
            restTemplate.delete(consentUrl);
            System.out.println("VBank: Согласие " + consentId + " успешно отозвано.");
        } catch (Exception e) { System.err.println("VBank: ОШИБКА ОТЗЫВА СОГЛАСИЯ! " + e.getMessage()); }
        finally { consentService.removeConsent(getBankName(), endUserClientId); }
    }

    private String getOrCreateConsent(String token, String endUserClientId) {
        String consentId = consentService.getConsent(getBankName(), endUserClientId);
        if (consentId != null) {
            System.out.println("VBank: Найдено существующее согласие " + consentId);
            return consentId;
        }
        return createAndSaveConsent(token, endUserClientId);
    }

    private String getAuthToken() {
        String authUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/auth/bank-token").queryParam("client_id", teamClientId).queryParam("client_secret", clientSecret).toUriString();
        try {
            ResponseEntity<VBankAuthResponse> response = restTemplate.postForEntity(authUrl, null, VBankAuthResponse.class);
            System.out.println("VBank: Токен успешно получен!");
            return response.getBody().getAccessToken();
        } catch (Exception e) { System.err.println("VBank: ОШИБКА АУТЕНТИФИКАЦИИ! " + e.getMessage()); return null; }
    }

    private String createAndSaveConsent(String token, String endUserClientId) {
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
            consentService.saveConsent(getBankName(), endUserClientId, consentId);
            return consentId;
        } catch (Exception e) { System.err.println("VBank: ОШИБКА СОЗДАНИЯ СОГЛАСИЯ! " + e.getMessage()); return null; }
    }

    private List<Account> getEnrichedAccounts(String token, String consentId, String endUserClientId) {
        String accountsUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/accounts").queryParam("client_id", endUserClientId).toUriString();
        HttpEntity<Void> requestEntity = buildHttpEntity(token, consentId);
        try {
            ResponseEntity<VBankAccountListResponseDto> response = restTemplate.exchange(accountsUrl, HttpMethod.GET, requestEntity, VBankAccountListResponseDto.class);
            List<VBankAccountDto> bankAccounts = response.getBody().getData().getAccount();
            if (bankAccounts == null) return Collections.emptyList();
            System.out.println("VBank: Успешно получено базовых счетов: " + bankAccounts.size());

            final String finalConsentId = consentId; // Создаем effectively final переменную
            return bankAccounts.stream().map(basicAccount -> {
                Account enrichedAccount = new Account(basicAccount.getAccountId(), getBankName());
                enrichedAccount.setAccountType(basicAccount.getAccountType());
                getAccountBalance(token, finalConsentId, endUserClientId, basicAccount.getAccountId())
                        .ifPresent(enrichedAccount::setBalance);
                return enrichedAccount;
            }).collect(Collectors.toList());
        } catch (Exception e) { System.err.println("VBank: ОШИБКА ПОЛУЧЕНИЯ СПИСКА СЧЕТОВ! " + e.getMessage()); return Collections.emptyList(); }
    }

    private Optional<Double> getAccountBalance(String token, String consentId, String endUserClientId, String accountId) {
        String balanceUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/accounts/" + accountId + "/balances").queryParam("client_id", endUserClientId).toUriString();
        HttpEntity<Void> requestEntity = buildHttpEntity(token, consentId);
        try {
            ResponseEntity<VBankBalanceResponseDto> response = restTemplate.exchange(balanceUrl, HttpMethod.GET, requestEntity, VBankBalanceResponseDto.class);
            return response.getBody().getData().getBalance().stream()
                    .filter(b -> "InterimAvailable".equalsIgnoreCase(b.getType()))
                    .findFirst()
                    .map(b -> Double.parseDouble(b.getAmount().getAmount()));
        } catch (Exception e) { System.err.println("VBank: ОШИБКА ПОЛУЧЕНИЯ БАЛАНСА для счета " + accountId + "! " + e.getMessage()); return Optional.empty(); }
    }

    private List<Transaction> getTransactions(String token, String consentId, String endUserClientId, String accountId) {
        String transactionsUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/accounts/" + accountId + "/transactions").queryParam("client_id", endUserClientId).toUriString();
        HttpEntity<Void> requestEntity = buildHttpEntity(token, consentId);
        try {
            ResponseEntity<VBankTransactionListResponseDto> response = restTemplate.exchange(transactionsUrl, HttpMethod.GET, requestEntity, VBankTransactionListResponseDto.class);
            List<VBankTransactionDto> bankTransactions = response.getBody().getData().getTransaction();
            if (bankTransactions == null) return Collections.emptyList();
            System.out.println("VBank: Для счета " + accountId + " получено транзакций: " + bankTransactions.size());
            return bankTransactions.stream()
                    .map(bankDto -> new Transaction(
                            bankDto.getTransactionInformation(),
                            Double.parseDouble(bankDto.getAmount().getAmount()),
                            bankDto.getBookingDateTime()
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) { System.err.println("VBank: ОШИБКА ПОЛУЧЕНИЯ ТРАНЗАКЦИЙ для счета " + accountId + "! " + e.getMessage()); return Collections.emptyList(); }
    }

    private HttpEntity<Void> buildHttpEntity(String token, String consentId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.set("X-Requesting-Bank", teamClientId);
        headers.set("X-Consent-Id", consentId);
        return new HttpEntity<>(headers);
    }
}