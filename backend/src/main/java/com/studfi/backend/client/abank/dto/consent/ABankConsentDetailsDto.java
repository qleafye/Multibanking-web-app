package com.studfi.backend.client.abank.dto.consent;

// Этот DTO соответствует ответу от GET /account-consents/{id}
// Мы создаем его для полноты, хотя для фронтенда можем возвращать и простой String
public class ABankConsentDetailsDto {
    private Data data;

    public static class Data {
        private String consentId;
        private String status;
        // ... можно добавить остальные поля из документации (permissions, expirationDateTime и т.д.)

        public String getConsentId() { return consentId; }
        public void setConsentId(String consentId) { this.consentId = consentId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public Data getData() { return data; }
    public void setData(Data data) { this.data = data; }
}