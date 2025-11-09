package com.client;

import com.dto.Account;
import com.dto.Product; // <-- ВОТ ЧЕГО НЕ ХВАТАЛО
import com.dto.Transaction;
import java.util.List;

public interface BankClient {
    String getBankName();
    List<Transaction> getAllTransactionsForClient(String endUserClientId);
    List<Product> getAvailableProducts();
    List<Account> getEnrichedAccountsForClient(String endUserClientId);
}