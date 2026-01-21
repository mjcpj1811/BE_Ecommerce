package com.example.BE_E_commerce.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "product_variants", indexes = {
        @Index(name = "idx_product_id", columnList = "product_id"),
        @Index(name = "idx_sku", columnList = "sku")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(unique = true, nullable = false, length = 100)
    private String sku;

    @Column(length = 50)
    private String size;

    @Column(length = 50)
    private String color;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(precision = 15, scale = 2)
    private BigDecimal originalPrice; // Giá gốc (trước khi giảm)

    @Column(name = "discount_percent")
    private Integer discountPercent;

    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // ========== HELPER METHODS ==========

    /**
     * Decrease stock
     */
    public void decreaseStock(Integer quantity) {
        if (this.stockQuantity < quantity) {
            throw new IllegalStateException("Not enough stock");
        }
        this.stockQuantity -= quantity;
    }

    /**
     * Increase stock
     */
    public void increaseStock(Integer quantity) {
        this.stockQuantity += quantity;
    }

    /**
     * Check if in stock
     */
    public boolean isInStock() {
        return this.stockQuantity > 0 && this.isActive;
    }
}