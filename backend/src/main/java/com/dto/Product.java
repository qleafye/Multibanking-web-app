package com.dto;

// Наш внутренний, унифицированный класс для банковского продукта
public class Product {
    private String productId;
    private String productType;
    private String productName;
    private String description;

    // Геттеры и сеттеры для всех полей
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}