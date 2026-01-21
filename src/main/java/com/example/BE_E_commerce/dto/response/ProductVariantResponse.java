package com.example.BE_E_commerce.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductVariantResponse {
    private Long id;
    private String sku;
    private String size;
    private String color;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer discountPercent;
    private Integer stockQuantity;
    private Boolean isActive;
}