package com.studfi.backend.client.sbank;

import com.studfi.backend.client.BankClient;
import com.studfi.backend.dto.Transaction;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;

@Component
public class SBankApiClient implements BankClient {
    @Override
    public String getBankName() { return "SBank"; }

    // И здесь тоже новый метод, который соответствует контракту
    @Override
    public List<Transaction> getAllTransactionsForClient(String endUserClientId) {
        System.out.println("ЗАПРОС К API SBANK: (Заглушка) для клиента " + endUserClientId);
        return Collections.emptyList(); // Возвращаем пустой список
    }
}