package com.client.studfi;

import com.client.BankClient;
import com.client.studfi.dto.account.StudFiAccountDto;
import com.client.studfi.dto.account.StudFiAccountListResponseDto;
import com.client.studfi.dto.account.StudFiBalanceResponseDto;
import com.client.studfi.dto.auth.StudFiAuthResponse;
import com.client.studfi.dto.consent.StudFiConsentDetailsDto;
import com.client.studfi.dto.consent.StudFiConsentRequest;
import com.client.studfi.dto.consent.StudFiConsentResponse;
import com.client.studfi.dto.product.StudFiProductDto;
import com.client.studfi.dto.product.StudFiProductListResponseDto;
import com.client.studfi.dto.transaction.StudFiTransactionDto;
import com.client.studfi.dto.transaction.StudFiTransactionListResponseDto;
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
public class StudFiApiClient implements BankClient {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired private ConsentService consentService;

    @Value("${studfi.api.baseurl}") private String baseUrl;
    @Value("${studfi.api.client-id}") private String teamClientId;
    @Value("${studfi.api.client-secret}") private String clientSecret;

    @Override
    public String getBankName() {
        // Это имя будет использоваться в ?banks=studfi
        return "studfi";
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
        System.out.println("StudFi: Запрашиваем каталог продуктов...");
        try {
            StudFiProductListResponseDto response = restTemplate.getForObject(productsUrl, StudFiProductListResponseDto.class);
            List<StudFiProductDto> bankProducts = response.getData().getProduct();
            if (bankProducts == null) return Collections.emptyList();
            System.out.println("StudFi: Успешно получено продуктов: " + bankProducts.size());
            return bankProducts.stream().map(bankProduct -> {
                Product product = new Product();
                product.setProductId(bankProduct.getProductId());
                product.setProductName(bankProduct.getProductName());
                product.setProductType(bankProduct.getProductType());
                product.setDescription(bankProduct.getDescription());
                return product;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("StudFi: ОШИБКА ПОЛУЧЕНИЯ КАТАЛОГА ПРОДУКТОВ! " + e.getMessage());
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

    public StudFiConsentDetailsDto getConsentDetails(String consentId) {
        String consentUrl = baseUrl + "/account-consents/" + consentId;
        try {
            return restTemplate.getForObject(consentUrl, StudFiConsentDetailsDto.class);
        } catch (Exception e) { System.err.println("StudFi: ОШИБКА ПОЛУЧЕНИЯ ДЕТАЛЕЙ СОГЛАСИЯ! " + e.getMessage()); return null; }
    }

    public void revokeConsent(String endUserClientId) {
        String consentId = consentService.getConsent(getBankName(), endUserClientId);
        if (consentId == null) return;
        String consentUrl = baseUrl + "/account-consents/" + consentId;
        try {
            restTemplate.delete(consentUrl);
            System.out.println("StudFi: Согласие " + consentId + " успешно отозвано.");
        } catch (Exception e) { System.err.println("StudFi: ОШИБКА ОТЗЫВА СОГЛАСИЯ! " + e.getMessage()); }
        finally { consentService.removeConsent(getBankName(), endUserClientId); }
    }

    private String getOrCreateConsent(String token, String endUserClientId) {
        String consentId = consentService.getConsent(getBankName(), endUserClientId);
        if (consentId != null) {
            System.out.println("StudFi: Найдено существующее согласие " + consentId);
            return consentId;
        }
        return createAndSaveConsent(token, endUserClientId);
    }

    private String getAuthToken() {
        String authUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/auth/bank-token").queryParam("client_id", teamClientId).queryParam("client_secret", clientSecret).toUriString();
        try {
            ResponseEntity<StudFiAuthResponse> response = restTemplate.postForEntity(authUrl, null, StudFiAuthResponse.class);
            System.out.println("StudFi: Токен успешно получен!");
            return response.getBody().getAccessToken();
        } catch (Exception e) { System.err.println("StudFi: ОШИБКА АУТЕНТИФИКАЦИИ! " + e.getMessage()); return null; }
    }

    private String createAndSaveConsent(String token, String endUserClientId) {
        String consentUrl = baseUrl + "/account-consents/request";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        headers.set("X-Requesting-Bank", teamClientId);
        StudFiConsentRequest requestBody = new StudFiConsentRequest(endUserClientId, teamClientId);
        HttpEntity<StudFiConsentRequest> requestEntity = new HttpEntity<>(requestBody, headers);
        try {
            ResponseEntity<StudFiConsentResponse> response = restTemplate.postForEntity(consentUrl, requestEntity, StudFiConsentResponse.class);
            String consentId = response.getBody().getConsentId();
            System.out.println("StudFi: Согласие для " + endUserClientId + " успешно создано! ID: " + consentId);
            consentService.saveConsent(getBankName(), endUserClientId, consentId);
            return consentId;
        } catch (Exception e) { System.err.println("StudFi: ОШИБКА СОЗДАНИЯ СОГЛАСИЯ! " + e.getMessage()); return null; }
    }

    private List<Account> getEnrichedAccounts(String token, String consentId, String endUserClientId) {
        String accountsUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/accounts").queryParam("client_id", endUserClientId).toUriString();
        HttpEntity<Void> requestEntity = buildHttpEntity(token, consentId);
        try {
            ResponseEntity<StudFiAccountListResponseDto> response = restTemplate.exchange(accountsUrl, HttpMethod.GET, requestEntity, StudFiAccountListResponseDto.class);
            List<StudFiAccountDto> bankAccounts = response.getBody().getData().getAccount();
            if (bankAccounts == null) return Collections.emptyList();
            System.out.println("StudFi: Успешно получено базовых счетов: " + bankAccounts.size());
            final String finalConsentId = consentId;
            return bankAccounts.stream().map(basicAccount -> {
                Account enrichedAccount = new Account(basicAccount.getAccountId(), getBankName());
                enrichedAccount.setAccountType(basicAccount.getAccountType());
                getAccountBalance(token, finalConsentId, endUserClientId, basicAccount.getAccountId())
                        .ifPresent(enrichedAccount::setBalance);
                return enrichedAccount;
            }).collect(Collectors.toList());
        } catch (Exception e) { System.err.println("StudFi: ОШИБКА ПОЛУЧЕНИЯ СПИСКА СЧЕТОВ! " + e.getMessage()); return Collections.emptyList(); }
    }

    private Optional<Double> getAccountBalance(String token, String consentId, String endUserClientId, String accountId) {
        String balanceUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/accounts/" + accountId + "/balances").queryParam("client_id", endUserClientId).toUriString();
        HttpEntity<Void> requestEntity = buildHttpEntity(token, consentId);
        try {
            ResponseEntity<StudFiBalanceResponseDto> response = restTemplate.exchange(balanceUrl, HttpMethod.GET, requestEntity, StudFiBalanceResponseDto.class);
            return response.getBody().getData().getBalance().stream()
                    .filter(b -> "InterimAvailable".equalsIgnoreCase(b.getType()))
                    .findFirst()
                    .map(b -> Double.parseDouble(b.getAmount().getAmount()));
        } catch (Exception e) { System.err.println("StudFi: ОШИБКА ПОЛУЧЕНИЯ БАЛАНСА для счета " + accountId + "! " + e.getMessage()); return Optional.empty(); }
    }

    private List<Transaction> getTransactions(String token, String consentId, String endUserClientId, String accountId) {
        String transactionsUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/accounts/" + accountId + "/transactions").queryParam("client_id", endUserClientId).toUriString();
        HttpEntity<Void> requestEntity = buildHttpEntity(token, consentId);
        try {
            ResponseEntity<StudFiTransactionListResponseDto> response = restTemplate.exchange(transactionsUrl, HttpMethod.GET, requestEntity, StudFiTransactionListResponseDto.class);
            List<StudFiTransactionDto> bankTransactions = response.getBody().getData().getTransaction();
            if (bankTransactions == null) return Collections.emptyList();
            System.out.println("StudFi: Для счета " + accountId + " получено транзакций: " + bankTransactions.size());
            return bankTransactions.stream()
                    .map(bankDto -> new Transaction(
                            bankDto.getTransactionInformation(),
                            Double.parseDouble(bankDto.getAmount().getAmount()),
                            bankDto.getBookingDateTime()
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) { System.err.println("StudFi: ОШИБКА ПОЛУЧЕНИЯ ТРАНЗАКЦИЙ для счета " + accountId + "! " + e.getMessage()); return Collections.emptyList(); }
    }

    private HttpEntity<Void> buildHttpEntity(String token, String consentId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.set("X-Requesting-Bank", teamClientId);
        headers.set("X-Consent-Id", consentId);
        return new HttpEntity<>(headers);
    }
}