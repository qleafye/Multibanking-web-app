package com.studfi.backend.client.abank.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ABankAuthResponse {
    @JsonProperty("access_token")
    private String accessToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}