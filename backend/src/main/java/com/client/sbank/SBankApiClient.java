package com.client.sbank;

import com.client.BankClient;
import com.client.sbank.dto.account.SBankAccountDto;
import com.client.sbank.dto.account.SBankAccountListResponseDto;
import com.client.sbank.dto.account.SBankBalanceResponseDto;
import com.client.sbank.dto.consent.SBankConsentDetailsDto;
import com.client.sbank.dto.consent.SBankConsentRequest;
import com.client.sbank.dto.consent.SBankConsentResponse;
import com.client.sbank.dto.transaction.SBankTransactionDto;
import com.client.sbank.dto.transaction.SBankTransactionListResponseDto;
import com.client.sbank.dto.account.*;
import com.client.sbank.dto.auth.SBankAuthResponse;
import com.client.sbank.dto.consent.*;
import com.client.sbank.dto.product.SBankProductDto;
import com.client.sbank.dto.product.SBankProductListResponseDto;
import com.client.sbank.dto.transaction.*;
import com.dto.Account;
import com.dto.Product;
import com.dto.Transaction;
import com.service.ConsentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SBankApiClient implements BankClient {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired private ConsentService consentService;

    @Value("${sbank.api.baseurl}") private String baseUrl;
    @Value("${sbank.api.client-id}") private String teamClientId;
    @Value("${sbank.api.client-secret}") private String clientSecret;

    @Override
    public String getBankName() {
        return "SBank";
    }

    // --- Все методы ниже являются копией ABankApiClient, но с логами и настройками для SBank ---

    @Override
    public List<Transaction> getAllTransactionsForClient(String endUserClientId) {
        String token = getAuthToken();
        if (token == null) return Collections.emptyList();
        String consentId = getOrCreateConsent(token, endUserClientId);
        if (consentId == null) return Collections.emptyList();
        List<Account> accounts = getEnrichedAccounts(token, consentId, endUserClientId);
        if (accounts.isEmpty()) return Collections.emptyList();
        final String finalConsentId = consentId;
        return accounts.stream()
                .flatMap(account -> getTransactions(token, finalConsentId, endUserClientId, account.getAccountId()).stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> getAvailableProducts() {
        String productsUrl = baseUrl + "/products";
        System.out.println("SBank: Запрашиваем каталог продуктов...");
        try {
            // Предполагаем, что структура ответа для продуктов у SBank тоже сложная
            SBankProductListResponseDto response = restTemplate.getForObject(productsUrl, SBankProductListResponseDto.class);
            List<SBankProductDto> bankProducts = response.getData().getProduct();
            if (bankProducts == null) return Collections.emptyList();
            System.out.println("SBank: Успешно получено продуктов: " + bankProducts.size());
            return bankProducts.stream().map(bankProduct -> {
                Product product = new Product();
                product.setProductId(bankProduct.getProductId());
                product.setProductName(bankProduct.getProductName());
                product.setProductType(bankProduct.getProductType());
                product.setDescription(bankProduct.getDescription());
                return product;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("SBank: ОШИБКА ПОЛУЧЕНИЯ КАТАЛОГА ПРОДУКТОВ! " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<Account> getEnrichedAccountsForClient(String endUserClientId) {
        String token = getAuthToken();
        if (token == null) return Collections.emptyList();
        String consentId = getOrCreateConsent(token, endUserClientId);
        if (consentId == null) return Collections.emptyList();
        return getEnrichedAccounts(token, consentId, endUserClientId);
    }

    public SBankConsentDetailsDto getConsentDetails(String consentId) {
        String consentUrl = baseUrl + "/account-consents/" + consentId;
        try {
            return restTemplate.getForObject(consentUrl, SBankConsentDetailsDto.class);
        } catch (Exception e) { System.err.println("SBank: ОШИБКА ПОЛУЧЕНИЯ ДЕТАЛЕЙ СОГЛАСИЯ! " + e.getMessage()); return null; }
    }

    public void revokeConsent(String endUserClientId) {
        String consentId = consentService.getConsent(getBankName(), endUserClientId);
        if (consentId == null) return;
        String consentUrl = baseUrl + "/account-consents/" + consentId;
        try {
            restTemplate.delete(consentUrl);
            System.out.println("SBank: Согласие " + consentId + " успешно отозвано.");
        } catch (Exception e) { System.err.println("SBank: ОШИБКА ОТЗЫВА СОГЛАСИЯ! " + e.getMessage()); }
        finally { consentService.removeConsent(getBankName(), endUserClientId); }
    }

    private String getOrCreateConsent(String token, String endUserClientId) {
        String consentId = consentService.getConsent(getBankName(), endUserClientId);
        if (consentId != null) {
            System.out.println("SBank: Найдено существующее согласие " + consentId);
            return consentId;
        }
        return createAndSaveConsent(token, endUserClientId);
    }

    private String getAuthToken() {
        String authUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/auth/bank-token").queryParam("client_id", teamClientId).queryParam("client_secret", clientSecret).toUriString();
        try {
            ResponseEntity<SBankAuthResponse> response = restTemplate.postForEntity(authUrl, null, SBankAuthResponse.class);
            System.out.println("SBank: Токен успешно получен!");
            return response.getBody().getAccessToken();
        } catch (Exception e) { System.err.println("SBank: ОШИБКА АУТЕНТИФИКАЦИИ! " + e.getMessage()); return null; }
    }

    private String createAndSaveConsent(String token, String endUserClientId) {
        String consentUrl = baseUrl + "/account-consents/request";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        headers.set("X-Requesting-Bank", teamClientId);
        SBankConsentRequest requestBody = new SBankConsentRequest(endUserClientId, teamClientId);
        HttpEntity<SBankConsentRequest> requestEntity = new HttpEntity<>(requestBody, headers);
        try {
            ResponseEntity<SBankConsentResponse> response = restTemplate.postForEntity(consentUrl, requestEntity, SBankConsentResponse.class);
            String consentId = response.getBody().getConsentId();
            System.out.println("SBank: Согласие для " + endUserClientId + " успешно создано! ID: " + consentId);
            consentService.saveConsent(getBankName(), endUserClientId, consentId);
            return consentId;
        } catch (Exception e) {
            // --- НАШ "ШПИОНСКИЙ" КОД ДЛЯ ОТЛОВА ОТВЕТА ---
            String responseBody = "Тело ответа пустое или недоступно.";
            if (e instanceof HttpClientErrorException) {
                responseBody = ((HttpClientErrorException) e).getResponseBodyAsString();
            }
            System.err.println("====================== СЫРОЙ ОТВЕТ ОТ SBANK (С ОШИБКОЙ) ======================");
            System.err.println(responseBody);
            System.err.println("============================================================================");
            System.err.println("SBank: ОШИБКА СОЗДАНИЯ СОГЛАСИЯ! " + e.getMessage());
            return null;
        }
    }

    private List<Account> getEnrichedAccounts(String token, String consentId, String endUserClientId) {
        String accountsUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/accounts").queryParam("client_id", endUserClientId).toUriString();
        HttpEntity<Void> requestEntity = buildHttpEntity(token, consentId);
        try {
            ResponseEntity<SBankAccountListResponseDto> response = restTemplate.exchange(accountsUrl, HttpMethod.GET, requestEntity, SBankAccountListResponseDto.class);
            List<SBankAccountDto> bankAccounts = response.getBody().getData().getAccount();
            if (bankAccounts == null) return Collections.emptyList();
            System.out.println("SBank: Успешно получено базовых счетов: " + bankAccounts.size());
            final String finalConsentId = consentId;
            return bankAccounts.stream().map(basicAccount -> {
                Account enrichedAccount = new Account(basicAccount.getAccountId(), getBankName());
                enrichedAccount.setAccountType(basicAccount.getAccountType());
                getAccountBalance(token, finalConsentId, endUserClientId, basicAccount.getAccountId())
                        .ifPresent(enrichedAccount::setBalance);
                return enrichedAccount;
            }).collect(Collectors.toList());
        } catch (Exception e) { System.err.println("SBank: ОШИБКА ПОЛУЧЕНИЯ СПИСКА СЧЕТОВ! " + e.getMessage()); return Collections.emptyList(); }
    }

    private Optional<Double> getAccountBalance(String token, String consentId, String endUserClientId, String accountId) {
        String balanceUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/accounts/" + accountId + "/balances").queryParam("client_id", endUserClientId).toUriString();
        HttpEntity<Void> requestEntity = buildHttpEntity(token, consentId);
        try {
            ResponseEntity<SBankBalanceResponseDto> response = restTemplate.exchange(balanceUrl, HttpMethod.GET, requestEntity, SBankBalanceResponseDto.class);
            return response.getBody().getData().getBalance().stream()
                    .filter(b -> "InterimAvailable".equalsIgnoreCase(b.getType()))
                    .findFirst()
                    .map(b -> Double.parseDouble(b.getAmount().getAmount()));
        } catch (Exception e) { System.err.println("SBank: ОШИБКА ПОЛУЧЕНИЯ БАЛАНСА для счета " + accountId + "! " + e.getMessage()); return Optional.empty(); }
    }

    private List<Transaction> getTransactions(String token, String consentId, String endUserClientId, String accountId) {
        String transactionsUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/accounts/" + accountId + "/transactions").queryParam("client_id", endUserClientId).toUriString();
        HttpEntity<Void> requestEntity = buildHttpEntity(token, consentId);
        try {
            ResponseEntity<SBankTransactionListResponseDto> response = restTemplate.exchange(transactionsUrl, HttpMethod.GET, requestEntity, SBankTransactionListResponseDto.class);
            List<SBankTransactionDto> bankTransactions = response.getBody().getData().getTransaction();
            if (bankTransactions == null) return Collections.emptyList();
            System.out.println("SBank: Для счета " + accountId + " получено транзакций: " + bankTransactions.size());
            return bankTransactions.stream()
                    .map(bankDto -> {
                        // 1. Сначала получаем сумму. Она всегда положительная.
                        double amount = Double.parseDouble(bankDto.getAmount().getAmount());

                        // 2. Проверяем, является ли транзакция списанием.
                        if ("Debit".equalsIgnoreCase(bankDto.getCreditDebitIndicator())) {
                            // 3. Если это списание, делаем сумму отрицательной
                            amount = -amount;
                        }

                        // 4. Возвращаем новый объект Transaction с уже правильным знаком суммы
                        return new Transaction(
                                bankDto.getTransactionInformation(),
                                amount,
                                bankDto.getBookingDateTime(),
                                getBankName()
                        );
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) { System.err.println("SBank: ОШИБКА ПОЛУЧЕНИЯ ТРАНЗАКЦИЙ для счета " + accountId + "! " + e.getMessage()); return Collections.emptyList(); }
    }

    private HttpEntity<Void> buildHttpEntity(String token, String consentId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.set("X-Requesting-Bank", teamClientId);
        headers.set("X-Consent-Id", consentId);
        return new HttpEntity<>(headers);
    }
}