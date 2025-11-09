package com.client.vbank.dto.account;

// Этот класс описывает один элемент в списке балансов
public class VBankBalanceDto {
    private AmountDto amount;
    private String type; // Например, "InterimAvailable"

    public static class AmountDto {
        private String amount;
        private String currency;
        public String getAmount() { return amount; }
        public void setAmount(String amount) { this.amount = amount; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }

    public AmountDto getAmount() { return amount; }
    public void setAmount(AmountDto amount) { this.amount = amount; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}