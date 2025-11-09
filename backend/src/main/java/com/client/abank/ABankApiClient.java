package com.client.abank;

import com.client.BankClient;
import com.client.abank.dto.account.ABankAccountDto;
import com.client.abank.dto.account.ABankAccountListResponseDto;
import com.client.abank.dto.account.ABankBalanceResponseDto;
import com.client.abank.dto.consent.ABankConsentDetailsDto;
import com.client.abank.dto.consent.ABankConsentRequest;
import com.client.abank.dto.consent.ABankConsentResponse;
import com.client.abank.dto.transaction.ABankTransactionDto;
import com.client.abank.dto.transaction.ABankTransactionListResponseDto;
import com.client.abank.dto.account.*;
import com.client.abank.dto.auth.ABankAuthResponse;
import com.client.abank.dto.consent.*;
import com.client.abank.dto.product.ABankProductDto;
import com.client.abank.dto.transaction.*;
import com.client.abank.dto.product.ABankProductListResponseDto;
import com.dto.Account;
import com.dto.Product;
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
public class ABankApiClient implements BankClient {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired private ConsentService consentService;

    @Value("${abank.api.baseurl}") private String baseUrl;
    @Value("${abank.api.client-id}") private String teamClientId;
    @Value("${abank.api.client-secret}") private String clientSecret;

    @Override
    public String getBankName() {
        return "ABank";
    }

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
        System.out.println("ABank: Запрашиваем каталог продуктов...");
        try {
            ABankProductListResponseDto response = restTemplate.getForObject(productsUrl, ABankProductListResponseDto.class);
            List<ABankProductDto> bankProducts = response.getData().getProduct();
            if (bankProducts == null) return Collections.emptyList();
            System.out.println("ABank: Успешно получено продуктов: " + bankProducts.size());
            return bankProducts.stream().map(bankProduct -> {
                Product product = new Product();
                product.setProductId(bankProduct.getProductId());
                product.setProductName(bankProduct.getProductName());
                product.setProductType(bankProduct.getProductType());
                product.setDescription(bankProduct.getDescription());
                return product;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("ABank: ОШИБКА ПОЛУЧЕНИЯ КАТАЛОГА ПРОДУКТОВ! " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<Account> getEnrichedAccountsForClient(String endUserClientId) {
        String token = getAuthToken();
        if (token == null) return Collections.emptyList();
        String consentId = getOrCreateConsent(token, endUserClientId);
        if (consentId == null) return Collections.emptyList();
        return getEnrichedAccounts(token, consentId, endUserClientId);
    }

    public ABankConsentDetailsDto getConsentDetails(String consentId) {
        String consentUrl = baseUrl + "/account-consents/" + consentId;
        try {
            return restTemplate.getForObject(consentUrl, ABankConsentDetailsDto.class);
        } catch (Exception e) { System.err.println("ABank: ОШИБКА ПОЛУЧЕНИЯ ДЕТАЛЕЙ СОГЛАСИЯ! " + e.getMessage()); return null; }
    }

    public void revokeConsent(String endUserClientId) {
        String consentId = consentService.getConsent(getBankName(), endUserClientId);
        if (consentId == null) return;
        String consentUrl = baseUrl + "/account-consents/" + consentId;
        try {
            restTemplate.delete(consentUrl);
            System.out.println("ABank: Согласие " + consentId + " успешно отозвано.");
        } catch (Exception e) { System.err.println("ABank: ОШИБКА ОТЗЫВА СОГЛАСИЯ! " + e.getMessage()); }
        finally { consentService.removeConsent(getBankName(), endUserClientId); }
    }

    private String getOrCreateConsent(String token, String endUserClientId) {
        String consentId = consentService.getConsent(getBankName(), endUserClientId);
        if (consentId != null) {
            System.out.println("ABank: Найдено существующее согласие " + consentId);
            return consentId;
        }
        return createAndSaveConsent(token, endUserClientId);
    }

    private String getAuthToken() {
        String authUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/auth/bank-token").queryParam("client_id", teamClientId).queryParam("client_secret", clientSecret).toUriString();
        try {
            ResponseEntity<ABankAuthResponse> response = restTemplate.postForEntity(authUrl, null, ABankAuthResponse.class);
            System.out.println("ABank: Токен успешно получен!");
            return response.getBody().getAccessToken();
        } catch (Exception e) { System.err.println("ABank: ОШИБКА АУТЕНТИФИКАЦИИ! " + e.getMessage()); return null; }
    }

    private String createAndSaveConsent(String token, String endUserClientId) {
        String consentUrl = baseUrl + "/account-consents/request";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        headers.set("X-Requesting-Bank", teamClientId);
        ABankConsentRequest requestBody = new ABankConsentRequest(endUserClientId, teamClientId);
        HttpEntity<ABankConsentRequest> requestEntity = new HttpEntity<>(requestBody, headers);
        try {
            ResponseEntity<ABankConsentResponse> response = restTemplate.postForEntity(consentUrl, requestEntity, ABankConsentResponse.class);
            String consentId = response.getBody().getConsentId();
            System.out.println("ABank: Согласие для " + endUserClientId + " успешно создано! ID: " + consentId);
            consentService.saveConsent(getBankName(), endUserClientId, consentId);
            return consentId;
        } catch (Exception e) { System.err.println("ABank: ОШИБКА СОЗДАНИЯ СОГЛАСИЯ! " + e.getMessage()); return null; }
    }

    private List<Account> getEnrichedAccounts(String token, String consentId, String endUserClientId) {
        String accountsUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/accounts").queryParam("client_id", endUserClientId).toUriString();
        HttpEntity<Void> requestEntity = buildHttpEntity(token, consentId);
        try {
            ResponseEntity<ABankAccountListResponseDto> response = restTemplate.exchange(accountsUrl, HttpMethod.GET, requestEntity, ABankAccountListResponseDto.class);
            List<ABankAccountDto> bankAccounts = response.getBody().getData().getAccount();
            if (bankAccounts == null) return Collections.emptyList();
            System.out.println("ABank: Успешно получено базовых счетов: " + bankAccounts.size());
            final String finalConsentId = consentId;
            return bankAccounts.stream().map(basicAccount -> {
                Account enrichedAccount = new Account(basicAccount.getAccountId(), getBankName());
                enrichedAccount.setAccountType(basicAccount.getAccountType());
                getAccountBalance(token, finalConsentId, endUserClientId, basicAccount.getAccountId())
                        .ifPresent(enrichedAccount::setBalance);
                return enrichedAccount;
            }).collect(Collectors.toList());
        } catch (Exception e) { System.err.println("ABank: ОШИБКА ПОЛУЧЕНИЯ СПИСКА СЧЕТОВ! " + e.getMessage()); return Collections.emptyList(); }
    }

    private Optional<Double> getAccountBalance(String token, String consentId, String endUserClientId, String accountId) {
        String balanceUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/accounts/" + accountId + "/balances").queryParam("client_id", endUserClientId).toUriString();
        HttpEntity<Void> requestEntity = buildHttpEntity(token, consentId);
        try {
            ResponseEntity<ABankBalanceResponseDto> response = restTemplate.exchange(balanceUrl, HttpMethod.GET, requestEntity, ABankBalanceResponseDto.class);
            return response.getBody().getData().getBalance().stream()
                    .filter(b -> "InterimAvailable".equalsIgnoreCase(b.getType()))
                    .findFirst()
                    .map(b -> Double.parseDouble(b.getAmount().getAmount()));
        } catch (Exception e) { System.err.println("ABank: ОШИБКА ПОЛУЧЕНИЯ БАЛАНСА для счета " + accountId + "! " + e.getMessage()); return Optional.empty(); }
    }

    private List<Transaction> getTransactions(String token, String consentId, String endUserClientId, String accountId) {
        String transactionsUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/accounts/" + accountId + "/transactions").queryParam("client_id", endUserClientId).toUriString();
        HttpEntity<Void> requestEntity = buildHttpEntity(token, consentId);
        try {
            ResponseEntity<ABankTransactionListResponseDto> response = restTemplate.exchange(transactionsUrl, HttpMethod.GET, requestEntity, ABankTransactionListResponseDto.class);
            List<ABankTransactionDto> bankTransactions = response.getBody().getData().getTransaction();
            if (bankTransactions == null) return Collections.emptyList();
            System.out.println("ABank: Для счета " + accountId + " получено транзакций: " + bankTransactions.size());
            return bankTransactions.stream()
                    .map(bankDto -> new Transaction(
                            bankDto.getTransactionInformation(),
                            Double.parseDouble(bankDto.getAmount().getAmount()),
                            bankDto.getBookingDateTime()
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) { System.err.println("ABank: ОШИБКА ПОЛУЧЕНИЯ ТРАНЗАКЦИЙ для счета " + accountId + "! " + e.getMessage()); return Collections.emptyList(); }
    }

    private HttpEntity<Void> buildHttpEntity(String token, String consentId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.set("X-Requesting-Bank", teamClientId);
        headers.set("X-Consent-Id", consentId);
        return new HttpEntity<>(headers);
    }
}