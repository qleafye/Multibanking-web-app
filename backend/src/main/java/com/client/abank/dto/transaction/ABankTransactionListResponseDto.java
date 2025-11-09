package com.client.abank.dto.transaction;

// "Внешняя коробка" для транзакций
public class ABankTransactionListResponseDto {
    private ABankTransactionDataDto data;

    public ABankTransactionDataDto getData() {
        return data;
    }

    public void setData(ABankTransactionDataDto data) {
        this.data = data;
    }
}