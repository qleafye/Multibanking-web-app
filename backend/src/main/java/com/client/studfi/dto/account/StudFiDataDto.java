package com.client.studfi.dto.account;

import java.util.List;

// Этот класс представляет средний объект {"account": [...]}
public class StudFiDataDto {

    private List<StudFiAccountDto> account; // Название поля 'account', как в JSON

    public List<StudFiAccountDto> getAccount() {
        return account;
    }

    public void setAccount(List<StudFiAccountDto> account) {
        this.account = account;
    }
}