package com.client.sbank.dto.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// "Средняя коробка" для транзакций
public class SBankTransactionDataDto {
    // В JSON ответах API часто используется единственное число для названия массива
    @JsonProperty("transaction")
    private List<SBankTransactionDto> transaction;

    public List<SBankTransactionDto> getTransaction() {
        return transaction;
    }

    public void setTransaction(List<SBankTransactionDto> transaction) {
        this.transaction = transaction;
    }
}