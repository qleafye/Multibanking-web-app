package com.client.abank.dto.account;

// Этот класс представляет самый внешний объект {"data": {...}}
public class ABankAccountListResponseDto {

    // Внутри него лежит объект VBankDataDto
    private ABankDataDto data;

    public ABankDataDto getData() {
        return data;
    }

    public void setData(ABankDataDto data) {
        this.data = data;
    }
}