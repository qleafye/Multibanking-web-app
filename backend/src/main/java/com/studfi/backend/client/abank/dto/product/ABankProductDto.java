package com.studfi.backend.client.abank.dto.product;

public class ABankProductDto {
    private String productId;
    private String productType;
    private String productName;
    private String description;

    // --- НЕДОСТАЮЩИЕ ГЕТТЕРЫ И СЕТТЕРЫ ---
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}