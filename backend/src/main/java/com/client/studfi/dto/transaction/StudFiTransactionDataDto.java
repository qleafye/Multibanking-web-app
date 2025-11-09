package com.client.studfi.dto.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// "Средняя коробка" для транзакций
public class StudFiTransactionDataDto {
    // В JSON ответах API часто используется единственное число для названия массива
    @JsonProperty("transaction")
    private List<StudFiTransactionDto> transaction;

    public List<StudFiTransactionDto> getTransaction() {
        return transaction;
    }

    public void setTransaction(List<StudFiTransactionDto> transaction) {
        this.transaction = transaction;
    }
}