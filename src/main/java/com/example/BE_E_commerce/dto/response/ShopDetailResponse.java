package com.example.BE_E_commerce.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ShopDetailResponse extends ShopResponse implements Serializable {

    // Featured products (top 10)
    private List<ProductResponse> featuredProducts = new ArrayList<>();

    // Best sellers (top 5)
    private List<ProductResponse> bestSellers = new ArrayList<>();

    // New arrivals (top 5)
    private List<ProductResponse> newArrivals = new ArrayList<>();

    // Categories that shop has products in
    private List<CategorySummary> categories =  new ArrayList<>();

    @Data
    public static class CategorySummary {
        private Long id;
        private String name;
        private String slug;
        private Long productCount;
    }
}