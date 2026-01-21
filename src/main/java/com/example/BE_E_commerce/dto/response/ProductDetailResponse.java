package com.example.BE_E_commerce.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProductDetailResponse extends ProductResponse {

    // Additional details for product detail page
    private String specifications; // JSON string or formatted text

    // Related products
    private List<ProductResponse> relatedProducts = new ArrayList<>();

    // Recent reviews (top 5)
    private List<ReviewResponse> recentReviews = new ArrayList<>();
}