package com.studfi.backend.client.sbank.dto.consent;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SBankConsentRequest {
    @JsonProperty("client_id")
    private String clientId;
    private List<String> permissions;
    private String reason;
    @JsonProperty("requesting_bank")
    private String requestingBank;
    @JsonProperty("requesting_bank_name")
    private String requestingBankName;

    public SBankConsentRequest(String clientId, String requestingBank) {
        this.clientId = clientId;
        this.requestingBank = requestingBank;
        this.permissions = List.of("ReadAccountsDetail", "ReadBalances", "ReadTransactionsDetail");
        this.reason = "StudFi App analysis for Hackathon";
        this.requestingBankName = "StudFi Application (Team 100)";
    }

    // Геттеры и сеттеры
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getRequestingBank() { return requestingBank; }
    public void setRequestingBank(String requestingBank) { this.requestingBank = requestingBank; }
    public String getRequestingBankName() { return requestingBankName; }
    public void setRequestingBankName(String requestingBankName) { this.requestingBankName = requestingBankName; }
}