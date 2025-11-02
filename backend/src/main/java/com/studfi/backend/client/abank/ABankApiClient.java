package com.studfi.backend.client.abank;

import com.studfi.backend.client.BankClient;
import com.studfi.backend.dto.Transaction;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;

@Component
public class ABankApiClient implements BankClient {
    @Override
    public String getBankName() { return "ABank"; }

    // Вот новый метод, который соответствует контракту BankClient
    @Override
    public List<Transaction> getAllTransactionsForClient(String endUserClientId) {
        System.out.println("ЗАПРОС К API ABANK: (Заглушка) для клиента " + endUserClientId);
        return Collections.emptyList(); // Возвращаем пустой список
    }
}