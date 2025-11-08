package com.client.vbank.dto.account;

import java.util.List;

// Это "коробка в коробке" для балансов
public class VBankBalanceResponseDto {
    private Data data;

    public static class Data {
        private List<VBankBalanceDto> balance;
        public List<VBankBalanceDto> getBalance() { return balance; }
        public void setBalance(List<VBankBalanceDto> balance) { this.balance = balance; }
    }

    public Data getData() { return data; }
    public void setData(Data data) { this.data = data; }
}