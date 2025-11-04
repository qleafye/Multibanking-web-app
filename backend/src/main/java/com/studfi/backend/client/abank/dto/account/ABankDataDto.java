package com.studfi.backend.client.abank.dto.account;

import java.util.List;

// Этот класс представляет средний объект {"account": [...]}
public class ABankDataDto {

    private List<ABankAccountDto> account; // Название поля 'account', как в JSON

    public List<ABankAccountDto> getAccount() {
        return account;
    }

    public void setAccount(List<ABankAccountDto> account) {
        this.account = account;
    }
}