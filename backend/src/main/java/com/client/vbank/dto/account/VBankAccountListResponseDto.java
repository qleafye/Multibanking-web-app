package com.client.vbank.dto.account;

// Этот класс представляет самый внешний объект {"data": {...}}
public class VBankAccountListResponseDto {

    // Внутри него лежит объект VBankDataDto
    private VBankDataDto data;

    public VBankDataDto getData() {
        return data;
    }

    public void setData(VBankDataDto data) {
        this.data = data;
    }
}