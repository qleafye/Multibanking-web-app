package com.studfi.backend.client.vbank.dto.account;

public class VBankAccountDto {
    private String accountId;
    private String nickname;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}