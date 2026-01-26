package com.example.BE_E_commerce.dto.response;

import com.example.BE_E_commerce.enums.ShopStatus;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ShopResponse implements Serializable {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String logoUrl;
    private String bannerUrl;
    private ShopStatus status;

    // Owner info
    private Long ownerId;
    private String ownerName;

    // Statistics
    private Double rating;
    private Long totalReviews;
    private Long totalProducts;
    private Long totalSold;
    private BigDecimal commissionRate;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}