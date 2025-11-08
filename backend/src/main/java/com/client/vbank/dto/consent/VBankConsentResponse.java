package com.client.vbank.dto.consent;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VBankConsentResponse {
    @JsonProperty("consent_id")
    private String consentId;
    private String status;

    // --- ДОБАВЛЕННЫЕ ГЕТТЕРЫ И СЕТТЕРЫ ---
    public String getConsentId() { return consentId; }
    public void setConsentId(String consentId) { this.consentId = consentId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}