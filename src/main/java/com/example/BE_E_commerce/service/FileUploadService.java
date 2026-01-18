package com.example.BE_E_commerce.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.BE_E_commerce.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j. Slf4j;
import org. springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.folder}")
    private String folder;

    @Value("${cloudinary.max-file-size}")
    private long maxFileSize;

    @Value("${cloudinary.allowed-formats}")
    private String allowedFormats;

    /**
     * Upload avatar to Cloudinary
     */
    public Map<String, Object> uploadAvatar(MultipartFile file, Long userId) {
        try {
            // Validate file
            validateFile(file);

            // Generate unique filename
            String publicId = folder + "/user_" + userId + "_" + UUID.randomUUID();

            // Upload to Cloudinary
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "folder", folder,
                            "resource_type", "image"
                    )
            );

            log.info("Avatar uploaded successfully for user: {} - URL: {}", userId, uploadResult. get("secure_url"));

            return uploadResult;

        } catch (IOException e) {
            log.error("Error uploading avatar: {}", e.getMessage());
            throw new BadRequestException("Failed to upload avatar:  " + e.getMessage());
        }
    }

    /**
     * Delete avatar from Cloudinary
     */
    public void deleteAvatar(String publicId) {
        try {
            if (publicId != null && !publicId.isEmpty()) {
                Map<String, Object> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                log.info("Avatar deleted:  {} - Result: {}", publicId, result. get("result"));
            }
        } catch (IOException e) {
            log.error("Error deleting avatar: {}", e.getMessage());
            // Don't throw exception, just log (avatar might already be deleted)
        }
    }

    /**
     * Extract public ID from Cloudinary URL
     */
    public String extractPublicId(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }

        try {
            // Example URL: https://res.cloudinary.com/demo/image/upload/v1234567890/folder/user_123_uuid.jpg
            String[] parts = imageUrl.split("/upload/");
            if (parts. length > 1) {
                String pathWithVersion = parts[1];
                // Remove version prefix (v1234567890/)
                String path = pathWithVersion.replaceFirst("v\\d+/", "");
                // Remove file extension
                return path.substring(0, path.lastIndexOf('.'));
            }
        } catch (Exception e) {
            log.warn("Failed to extract public ID from URL: {}", imageUrl);
        }

        return null;
    }

    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        // Check file size
        if (file.getSize() > maxFileSize) {
            throw new BadRequestException("File size exceeds maximum limit of " + (maxFileSize / 1024 / 1024) + "MB");
        }

        // Check file format
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new BadRequestException("Invalid file type");
        }

        List<String> allowed = Arrays.asList(allowedFormats.split(","));
        String fileExtension = getFileExtension(file.getOriginalFilename());

        if (!allowed.contains(fileExtension. toLowerCase())) {
            throw new BadRequestException("File format not allowed.  Allowed formats: " + allowedFormats);
        }
    }

    /**
     * Get file extension
     */
    private String getFileExtension(String filename) {
        if (filename == null || ! filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * Upload product image
     */
    public Map<String, Object> uploadProductImage(MultipartFile file, Long productId) {
        try {
            validateFile(file);

            String publicId = "ecommerce/products/product_" + productId + "_" + UUID.randomUUID();

            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file. getBytes(),
                    ObjectUtils. asMap(
                            "public_id", publicId,
                            "folder", "ecommerce/products",
                            "resource_type", "image"
                    )
            );

            log.info("Product image uploaded:  {}", uploadResult.get("secure_url"));

            return uploadResult;

        } catch (IOException e) {
            log.error("Error uploading product image: {}", e.getMessage());
            throw new BadRequestException("Failed to upload image: " + e.getMessage());
        }
    }
}