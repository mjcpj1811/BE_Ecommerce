package com.example.BE_E_commerce.dto.request;

import lombok.Data;

@Data
public class ShopFilterRequest {

    // Search
    private String keyword;

    // Filters
    private String status; // ACTIVE, SUSPENDED, PENDING
    private Double minRating;

    // Sorting
    private String sortBy = "createdAt"; // createdAt, rating, totalSold, totalProducts
    private String sortDirection = "DESC"; // ASC, DESC

    // Pagination
    private Integer page = 0;
    private Integer size = 20;
}