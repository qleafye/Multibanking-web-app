package com.client.vbank.dto.transaction;

// "Внешняя коробка" для транзакций
public class VBankTransactionListResponseDto {
    private VBankTransactionDataDto data;

    public VBankTransactionDataDto getData() {
        return data;
    }

    public void setData(VBankTransactionDataDto data) {
        this.data = data;
    }
}