package com.client.abank.dto.account;

import java.util.List;

// Это "коробка в коробке" для балансов
public class ABankBalanceResponseDto {
    private Data data;

    public static class Data {
        private List<ABankBalanceDto> balance;
        public List<ABankBalanceDto> getBalance() { return balance; }
        public void setBalance(List<ABankBalanceDto> balance) { this.balance = balance; }
    }

    public Data getData() { return data; }
    public void setData(Data data) { this.data = data; }
}