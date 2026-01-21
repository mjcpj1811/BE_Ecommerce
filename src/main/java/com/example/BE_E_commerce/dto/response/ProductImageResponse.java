package com.example.BE_E_commerce.dto.response;


import lombok.Data;

@Data
public class ProductImageResponse {
    private Long id;
    private String imageUrl;
    private Integer displayOrder;
    private Boolean isPrimary;
}