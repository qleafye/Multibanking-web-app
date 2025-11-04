package com.studfi.backend.client;

import com.studfi.backend.dto.Account;
import com.studfi.backend.dto.Product; // <-- ВОТ ЧЕГО НЕ ХВАТАЛО
import com.studfi.backend.dto.Transaction;
import java.util.List;

public interface BankClient {
    String getBankName();
    List<Transaction> getAllTransactionsForClient(String endUserClientId);
    List<Product> getAvailableProducts();
    List<Account> getEnrichedAccountsForClient(String endUserClientId);
}