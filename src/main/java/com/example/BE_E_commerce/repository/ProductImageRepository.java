package com.example.BE_E_commerce.repository;

import com.example.BE_E_commerce.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage,Long> {
    // Find by product
    List<ProductImage> findByProductIdOrderByDisplayOrderAsc(Long productId);

    // Find primary image
    Optional<ProductImage> findByProductIdAndIsPrimaryTrue(Long productId);


    // Get first image (thumbnail)
    Optional<ProductImage> findFirstByProductId(@Param("productId") Long productId);
}
