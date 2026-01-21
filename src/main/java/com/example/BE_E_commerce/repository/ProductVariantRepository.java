package com.example.BE_E_commerce.repository;

import com.example.BE_E_commerce.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant,Long> {
    // Find by SKU
    Optional<ProductVariant> findBySku(String sku);

    // Find by product
    List<ProductVariant> findByProductId(Long productId);

    // Find active variants by product
    List<ProductVariant> findByProductIdAndIsActiveTrue(Long productId);

    // Get min price of product
    @Query("SELECT MIN(v.price) FROM ProductVariant v WHERE v.product.id = :productId AND v.isActive = true")
    BigDecimal findMinPriceByProductId(@Param("productId") Long productId);

    // Get max price of product
    @Query("SELECT MAX(v. price) FROM ProductVariant v WHERE v.product.id = :productId AND v.isActive = true")
    BigDecimal findMaxPriceByProductId(@Param("productId") Long productId);

    // Get total stock of product
    @Query("SELECT SUM(v.stockQuantity) FROM ProductVariant v WHERE v.product.id = :productId AND v.isActive = true")
    Integer sumStockByProductId(@Param("productId") Long productId);

    // Check SKU exists
    boolean existsBySku(String sku);
}
