package com.example.BE_E_commerce.dto.response;

import com.example.BE_E_commerce.enums.ProductStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProductResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private ProductStatus status;

    // Category
    private Long categoryId;
    private String categoryName;

    // Shop
    private Long shopId;
    private String shopName;

    // Pricing (from variants)
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal originalPrice;
    private Integer discountPercent;

    // Stats
    private Integer totalStock;
    private Long totalSold;
    private Double averageRating;
    private Long reviewCount;

    // Images
    private String thumbnailUrl;
    private List<ProductImageResponse> images = new ArrayList<>();

    // Variants
    private List<ProductVariantResponse> variants = new ArrayList<>();

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}