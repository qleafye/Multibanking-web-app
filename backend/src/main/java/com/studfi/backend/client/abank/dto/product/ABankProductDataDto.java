package com.studfi.backend.client.abank.dto.product;

import java.util.List;

public class ABankProductDataDto {
    // Поле называется 'product', как в JSON
    private List<ABankProductDto> product;

    // --- ИСПРАВЛЕННЫЙ МЕТОД: getProduct() вместо getProducts() ---
    public List<ABankProductDto> getProduct() {
        return product;
    }

    public void setProduct(List<ABankProductDto> product) {
        this.product = product;
    }
}