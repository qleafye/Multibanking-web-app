package com.client.vbank.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VBankAuthResponse {
    @JsonProperty("access_token")
    private String accessToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}