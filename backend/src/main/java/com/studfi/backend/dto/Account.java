package com.studfi.backend.dto;

// Наш внутренний, унифицированный класс для банковского счёта.
// Неважно, как счёт выглядит в API VBank или ABank, внутри нашей системы он будет таким.
public class Account {
    private String accountId;
    private String bankName; // Добавим поле, чтобы знать, из какого банка этот счёт

    // Конструкторы, геттеры и сеттеры - стандартные блоки для Java DTO
    public Account(String accountId, String bankName) {
        this.accountId = accountId;
        this.bankName = bankName;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
}