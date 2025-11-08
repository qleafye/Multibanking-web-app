package com.client.abank.dto.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// "Средняя коробка" для транзакций
public class ABankTransactionDataDto {
    // В JSON ответах API часто используется единственное число для названия массива
    @JsonProperty("transaction")
    private List<ABankTransactionDto> transaction;

    public List<ABankTransactionDto> getTransaction() {
        return transaction;
    }

    public void setTransaction(List<ABankTransactionDto> transaction) {
        this.transaction = transaction;
    }
}