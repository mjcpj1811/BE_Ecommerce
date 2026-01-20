package com.example.BE_E_commerce.dto.response;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class CategoryResponse implements Serializable {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String imageUrl;
    private Integer displayOrder;
    private Boolean isActive;
    private Long parentId;
    private String parentName;
    private Integer level;
    private Long productCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Subcategories
    private List<CategoryResponse> children = new ArrayList<>();
}

