package com.service;

import com.client.BankClient;
import com.dto.Transaction;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnalysisService {

    private final List<BankClient> bankClients;

    // Список клиентов, чьи данные мы хотим проанализировать.
    // Для MVP сфокусируемся на студенте, но можем добавить и других.
    private final List<String> targetClientIds = List.of("team100-5"); // 5 - это студент

    public AnalysisService(List<BankClient> bankClients) {
        this.bankClients = bankClients;
    }

    // --- НОВЫЙ МЕТОД, который принимает список банков для фильтрации ---
    public List<Transaction> getAllTransactionsFromAllBanks(List<String> requestedBanks) {
        List<BankClient> clientsToProcess;

        // --- ЛОГИКА ФИЛЬТРАЦИИ ---
        // Если параметр `banks` не пришел или пустой...
        if (requestedBanks == null || requestedBanks.isEmpty()) {
            // ...то используем ВСЕ доступные клиенты
            clientsToProcess = this.bankClients;
            System.out.println("Фильтр банков не применен. Работаем со всеми банками.");
        } else {
            // ...иначе, фильтруем наш список клиентов по именам
            // Делаем сравнение без учета регистра для надежности
            List<String> lowerCaseRequestedBanks = requestedBanks.stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());
            clientsToProcess = this.bankClients.stream()
                    .filter(client -> lowerCaseRequestedBanks.contains(client.getBankName().toLowerCase()))
                    .collect(Collectors.toList());
            System.out.println("Применен фильтр. Работаем с банками: " + requestedBanks);
        }

        List<Transaction> allTransactions = new ArrayList<>();

        // Пробегаемся уже по отфильтрованному списку клиентов
        for (BankClient client : clientsToProcess) {
            System.out.println("--- Работаем с банком: " + client.getBankName() + " ---");
            for (String endUserClientId : targetClientIds) {
                System.out.println("Получаем транзакции для клиента: " + endUserClientId);
                allTransactions.addAll(client.getAllTransactionsForClient(endUserClientId));
            }
        }
        System.out.println("Сбор транзакций завершен. Всего найдено: " + allTransactions.size());
        return allTransactions;
    }
}