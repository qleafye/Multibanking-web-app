package com.client.vbank.dto.account;

import java.util.List;

// Этот класс представляет средний объект {"account": [...]}
public class VBankDataDto {

    private List<VBankAccountDto> account; // Название поля 'account', как в JSON

    public List<VBankAccountDto> getAccount() {
        return account;
    }

    public void setAccount(List<VBankAccountDto> account) {
        this.account = account;
    }
}