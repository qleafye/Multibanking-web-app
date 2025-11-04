package com.studfi.backend.client.sbank.dto.product;

import java.util.List;

public class SBankProductDataDto {
    // Поле называется 'product', как в JSON
    private List<SBankProductDto> product;

    // --- ИСПРАВЛЕННЫЙ МЕТОД: getProduct() вместо getProducts() ---
    public List<SBankProductDto> getProduct() {
        return product;
    }

    public void setProduct(List<SBankProductDto> product) {
        this.product = product;
    }
}