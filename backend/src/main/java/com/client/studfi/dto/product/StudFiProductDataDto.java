package com.client.studfi.dto.product;

import java.util.List;

public class StudFiProductDataDto {
    // Поле называется 'product', как в JSON
    private List<StudFiProductDto> product;

    // --- ИСПРАВЛЕННЫЙ МЕТОД: getProduct() вместо getProducts() ---
    public List<StudFiProductDto> getProduct() {
        return product;
    }

    public void setProduct(List<StudFiProductDto> product) {
        this.product = product;
    }
}