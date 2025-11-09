package com.service;

import jakarta.annotation.PostConstruct; // <-- ПРАВИЛЬНЫЙ ИМПОРТ
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConsentService {

    private final Map<String, String> consentStore = new ConcurrentHashMap<>();

    @PostConstruct
    public void initPreApprovedConsents() {
        String sbankConsentId = "consent-7c01aebb6093";

        saveConsent("SBank", "team100-5", sbankConsentId);
        System.out.println("ХРАНИЛИЩЕ: Предзагружено одобренное согласие для SBank: " + sbankConsentId);
    }

    public void saveConsent(String bankName, String endUserClientId, String consentId) {
        String key = generateKey(bankName, endUserClientId);
        consentStore.put(key, consentId);
        System.out.println("ХРАНИЛИЩЕ: Согласие " + consentId + " для " + key + " сохранено.");
    }

    public String getConsent(String bankName, String endUserClientId) {
        String key = generateKey(bankName, endUserClientId);
        return consentStore.get(key);
    }

    public void removeConsent(String bankName, String endUserClientId) {
        String key = generateKey(bankName, endUserClientId);
        String removedId = consentStore.remove(key);
        if (removedId != null) {
            System.out.println("ХРАНИЛИЩЕ: Согласие " + removedId + " для " + key + " удалено.");
        }
    }

    private String generateKey(String bankName, String endUserClientId) {
        return bankName + "-" + endUserClientId;
    }
}