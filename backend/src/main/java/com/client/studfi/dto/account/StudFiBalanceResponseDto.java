package com.client.studfi.dto.account;

import java.util.List;

// Это "коробка в коробке" для балансов
public class StudFiBalanceResponseDto {
    private Data data;

    public static class Data {
        private List<StudFiBalanceDto> balance;
        public List<StudFiBalanceDto> getBalance() { return balance; }
        public void setBalance(List<StudFiBalanceDto> balance) { this.balance = balance; }
    }

    public Data getData() { return data; }
    public void setData(Data data) { this.data = data; }
}