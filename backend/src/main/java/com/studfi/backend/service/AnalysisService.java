package com.studfi.backend.service;

import com.studfi.backend.client.BankClient;
import com.studfi.backend.dto.Transaction;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnalysisService {

    private final List<BankClient> bankClients;

    // Список клиентов, чьи данные мы хотим проанализировать.
    // Для MVP сфокусируемся на студенте, но можем добавить и других.
    private final List<String> targetClientIds = List.of("team100-5"); // 5 - это студент

    public AnalysisService(List<BankClient> bankClients) {
        this.bankClients = bankClients;
    }

    public List<Transaction> getAllTransactionsFromAllBanks() {
        List<Transaction> allTransactions = new ArrayList<>();

        // Пробегаемся по всем банкам
        for (BankClient client : bankClients) {
            System.out.println("--- Работаем с банком: " + client.getBankName() + " ---");
            // Внутри каждого банка пробегаемся по всем нашим целевым клиентам
            for (String endUserClientId : targetClientIds) {
                System.out.println("Получаем транзакции для клиента: " + endUserClientId);
                allTransactions.addAll(client.getAllTransactionsForClient(endUserClientId));
            }
        }
        System.out.println("Сбор транзакций завершен. Всего найдено: " + allTransactions.size());
        return allTransactions;
    }
}