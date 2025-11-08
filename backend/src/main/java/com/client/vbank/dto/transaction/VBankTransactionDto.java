package com.client.vbank.dto.transaction;

public class VBankTransactionDto {

    // --- ИСПРАВЛЕННЫЕ "ЯРЛЫКИ" ---
    // Предполагаем, что в JSON от API поле называется "transactionInformation"
    private String transactionInformation;

    // Предполагаем, что сумма лежит в объекте "amount", а внутри него поле "amount"
    private AmountDto amount;

    // Предполагаем, что дата называется "bookingDateTime"
    private String bookingDateTime;

    // --- Геттеры и сеттеры для новых полей ---
    public String getTransactionInformation() { return transactionInformation; }
    public void setTransactionInformation(String transactionInformation) { this.transactionInformation = transactionInformation; }
    public AmountDto getAmount() { return amount; }
    public void setAmount(AmountDto amount) { this.amount = amount; }
    public String getBookingDateTime() { return bookingDateTime; }
    public void setBookingDateTime(String bookingDateTime) { this.bookingDateTime = bookingDateTime; }

    // Вложенный класс для суммы, т.к. она может быть объектом
    public static class AmountDto {
        private String amount;
        public String getAmount() { return amount; }
        public void setAmount(String amount) { this.amount = amount; }
    }
}