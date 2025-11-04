package com.studfi.backend.client.sbank.dto.account;

import java.util.List;

// Этот класс представляет средний объект {"account": [...]}
public class SBankDataDto {

    private List<SBankAccountDto> account; // Название поля 'account', как в JSON

    public List<SBankAccountDto> getAccount() {
        return account;
    }

    public void setAccount(List<SBankAccountDto> account) {
        this.account = account;
    }
}