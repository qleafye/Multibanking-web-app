package com.studfi.backend.controller;

import com.studfi.backend.client.BankClient;
import com.studfi.backend.client.vbank.VBankApiClient;
import com.studfi.backend.client.vbank.dto.consent.VBankConsentDetailsDto;
import com.studfi.backend.dto.Account;
import com.studfi.backend.dto.Transaction;
import com.studfi.backend.service.AnalysisService;
import com.studfi.backend.service.ConsentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class AnalysisController {

    private final AnalysisService analysisService;
    private final List<BankClient> bankClients;
    private final ConsentService consentService;

    @Autowired
    public AnalysisController(AnalysisService analysisService, List<BankClient> bankClients, ConsentService consentService) {
        this.analysisService = analysisService;
        this.bankClients = bankClients;
        this.consentService = consentService;
    }

    // 1. Эндпоинт для анализа транзакций (для Python)
    @GetMapping("/transactions")
    public List<Transaction> getTransactionsForAnalysis() {
        return analysisService.getAllTransactionsFromAllBanks();
    }

    // --- 2. НОВЫЙ ЭНДПОИНТ ДЛЯ ПОЛУЧЕНИЯ "ОБОГАЩЕННЫХ" СЧЕТОВ ---
    @GetMapping("/accounts/{clientId}")
    public ResponseEntity<List<Account>> getClientAccounts(@PathVariable String clientId) {
        // Пробегаемся по всем банкам и собираем "обогащенные" счета с каждого
        List<Account> allAccounts = bankClients.stream()
                .flatMap(client -> client.getEnrichedAccountsForClient(clientId).stream())
                .collect(Collectors.toList());

        return ResponseEntity.ok(allAccounts);
    }

    // 3. Эндпоинт для проверки статуса согласия
    @GetMapping("/consents/{bankName}/{clientId}/status")
    public ResponseEntity<?> getConsentStatus(@PathVariable String bankName, @PathVariable String clientId) {
        BankClient client = findBankClient(bankName);
        if (!(client instanceof VBankApiClient vbankClient)) {
            return ResponseEntity.badRequest().body("Банк " + bankName + " не поддерживается.");
        }

        String consentId = consentService.getConsent(bankName, clientId);
        if (consentId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Согласие не найдено в хранилище.");
        }

        VBankConsentDetailsDto details = vbankClient.getConsentDetails(consentId);
        if (details == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Не удалось получить детали согласия от банка.");
        }
        return ResponseEntity.ok(details);
    }

    // 4. Эндпоинт для отзыва согласия
    @DeleteMapping("/consents/{bankName}/{clientId}")
    public ResponseEntity<String> revokeConsent(@PathVariable String bankName, @PathVariable String clientId) {
        BankClient client = findBankClient(bankName);
        if (!(client instanceof VBankApiClient vbankClient)) {
            return ResponseEntity.badRequest().body("Банк " + bankName + " не поддерживается.");
        }

        vbankClient.revokeConsent(clientId);
        return ResponseEntity.ok("Запрос на отзыв согласия отправлен.");
    }

    // Вспомогательный метод для поиска нужного клиента
    private BankClient findBankClient(String bankName) {
        return bankClients.stream()
                .filter(c -> c.getBankName().equalsIgnoreCase(bankName))
                .findFirst()
                .orElse(null);
    }
}