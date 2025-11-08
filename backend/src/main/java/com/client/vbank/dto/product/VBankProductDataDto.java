package com.client.vbank.dto.product;

import java.util.List;

public class VBankProductDataDto {
    // Поле называется 'product', как в JSON
    private List<VBankProductDto> product;

    // --- ИСПРАВЛЕННЫЙ МЕТОД: getProduct() вместо getProducts() ---
    public List<VBankProductDto> getProduct() {
        return product;
    }

    public void setProduct(List<VBankProductDto> product) {
        this.product = product;
    }
}