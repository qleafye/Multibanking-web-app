package com.client.studfi.dto.account;

// Этот класс представляет самый внешний объект {"data": {...}}
public class StudFiAccountListResponseDto {

    // Внутри него лежит объект VBankDataDto
    private StudFiDataDto data;

    public StudFiDataDto getData() {
        return data;
    }

    public void setData(StudFiDataDto data) {
        this.data = data;
    }
}