package com.example.BE_E_commerce.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewResponse {
    private Long id;
    private Integer rating;
    private String comment;
    private String userName;
    private String userAvatar;
    private LocalDateTime createdAt;
}