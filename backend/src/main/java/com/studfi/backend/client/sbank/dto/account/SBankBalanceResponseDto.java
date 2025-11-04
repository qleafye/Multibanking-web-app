package com.studfi.backend.client.sbank.dto.account;

import java.util.List;

// Это "коробка в коробке" для балансов
public class SBankBalanceResponseDto {
    private Data data;

    public static class Data {
        private List<SBankBalanceDto> balance;
        public List<SBankBalanceDto> getBalance() { return balance; }
        public void setBalance(List<SBankBalanceDto> balance) { this.balance = balance; }
    }

    public Data getData() { return data; }
    public void setData(Data data) { this.data = data; }
}