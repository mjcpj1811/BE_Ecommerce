package com.example.BE_E_commerce.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadAvatarResponse {
    private String avatarUrl;
    private String publicId;
    private String format;
    private Long size;
    private Integer width;
    private Integer height;
}
