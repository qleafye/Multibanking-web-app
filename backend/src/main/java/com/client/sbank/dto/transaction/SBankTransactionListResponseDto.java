package com.client.sbank.dto.transaction;

// "Внешняя коробка" для транзакций
public class SBankTransactionListResponseDto {
    private SBankTransactionDataDto data;

    public SBankTransactionDataDto getData() {
        return data;
    }

    public void setData(SBankTransactionDataDto data) {
        this.data = data;
    }
}