package com.dto;

// Наша внутренняя, унифицированная транзакция.
public class Transaction {
    private String description;
    private double amount;
    private String date;
    private String bankName;

    // Конструкторы, геттеры и сеттеры
    public Transaction(String description, double amount, String date, String bankName) { // <-- ОБНОВЛЕННЫЙ КОНСТРУКТОР
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.bankName = bankName; // <-- ДОБАВЛЕНО
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getBankName() { return bankName; }

    public void setBankName(String bankName) { this.bankName = bankName; }
}