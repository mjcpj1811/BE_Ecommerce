package com.example.BE_E_commerce.entity;

import com.example.BE_E_commerce.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate. annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_category_id", columnList = "category_id"),
        @Index(name = "idx_shop_id", columnList = "shop_id"),
        @Index(name = "idx_slug", columnList = "slug"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_total_sold", columnList = "total_sold"),        // ← NEW
        @Index(name = "idx_average_rating", columnList = "average_rating") // ← NEW
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType. IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(unique = true, nullable = false, length = 255)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    // ========== STATISTICS FIELDS ========== (THÊM MỚI)

    /**
     * Total units sold (denormalized for performance)
     * Updated when order is completed
     */
    @Column(name = "total_sold", nullable = false)
    @Builder.Default
    private Long totalSold = 0L;

    /**
     * Average rating from reviews (denormalized for performance)
     * Recalculated when new review is approved
     */
    @Column(name = "average_rating", nullable = false, columnDefinition = "DECIMAL(3,2) DEFAULT 0.00")
    @Builder.Default
    private Double averageRating = 0.0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp  // ← THÊM nếu chưa có
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ========== RELATIONSHIPS ==========

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductVariant> variants = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    // ========== HELPER METHODS ==========

    /**
     * Increment total sold
     */
    public void incrementTotalSold(Integer quantity) {
        this.totalSold += quantity;
    }

    /**
     * Update average rating
     */
    public void updateAverageRating(Double newAverageRating) {
        this.averageRating = newAverageRating;
    }
}