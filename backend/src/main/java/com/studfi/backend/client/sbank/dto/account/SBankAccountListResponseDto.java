package com.studfi.backend.client.sbank.dto.account;

// Этот класс представляет самый внешний объект {"data": {...}}
public class SBankAccountListResponseDto {

    // Внутри него лежит объект VBankDataDto
    private SBankDataDto data;

    public SBankDataDto getData() {
        return data;
    }

    public void setData(SBankDataDto data) {
        this.data = data;
    }
}