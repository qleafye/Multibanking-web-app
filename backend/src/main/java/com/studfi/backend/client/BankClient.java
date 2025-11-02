package com.studfi.backend.client;

import com.studfi.backend.dto.Transaction;
import java.util.List;

// НОВЫЙ, ПРАВИЛЬНЫЙ КОНТРАКТ
public interface BankClient {
    String getBankName();

    /**
     * Получает ВСЕ транзакции для ОДНОГО конкретного клиента,
     * проходя полный цикл: токен -> согласие -> счета -> транзакции.
     * @param endUserClientId ID конечного клиента (например, "team100-5")
     * @return Список всех его транзакций в нашем унифицированном формате.
     */
    List<Transaction> getAllTransactionsForClient(String endUserClientId);
}