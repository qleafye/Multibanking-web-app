package com.client.studfi.dto.transaction;

// "Внешняя коробка" для транзакций
public class StudFiTransactionListResponseDto {
    private StudFiTransactionDataDto data;

    public StudFiTransactionDataDto getData() {
        return data;
    }

    public void setData(StudFiTransactionDataDto data) {
        this.data = data;
    }
}