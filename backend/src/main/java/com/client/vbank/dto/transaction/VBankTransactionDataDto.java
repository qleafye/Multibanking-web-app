package com.client.vbank.dto.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// "Средняя коробка" для транзакций
public class VBankTransactionDataDto {
    // В JSON ответах API часто используется единственное число для названия массива
    @JsonProperty("transaction")
    private List<VBankTransactionDto> transaction;

    public List<VBankTransactionDto> getTransaction() {
        return transaction;
    }

    public void setTransaction(List<VBankTransactionDto> transaction) {
        this.transaction = transaction;
    }
}