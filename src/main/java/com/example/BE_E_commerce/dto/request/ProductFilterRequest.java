package com.example.BE_E_commerce.dto.request;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductFilterRequest {

    // Search
    private String keyword;

    // Filters
    private Long categoryId;
    private Long shopId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Double minRating; // 0-5

    // Sorting
    private String sortBy = "createdAt"; // createdAt, price, sold, rating
    private String sortDirection = "DESC"; // ASC, DESC

    // Pagination
    private Integer page = 0;
    private Integer size = 20;
}
