package com.example.BE_E_commerce.entity;

import com.example.BE_E_commerce.enums.ShopStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org. hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shops", indexes = {
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_slug", columnList = "slug")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType. IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType. LAZY)
    @JoinColumn(name = "owner_id", unique = true, nullable = false)
    private User owner;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(unique = true, nullable = false, length = 255)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String logoUrl;

    @Column(length = 500)
    private String bannerUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ShopStatus status = ShopStatus.PENDING;

    @Column(precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalReviews = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalProducts = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalSold = 0;

    @Column(precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal commissionRate = new BigDecimal("5.00"); // 5%

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // ========== RELATIONSHIPS ==========

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Product> products = new ArrayList<>();

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ShopBankAccount> bankAccounts = new ArrayList<>();

    @OneToOne(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
    private ShopBalance balance;

    // ========== HELPER METHODS ==========

    public void addProduct(Product product) {
        products.add(product);
        product.setShop(this);
    }

    public void removeProduct(Product product) {
        products.remove(product);
        product.setShop(null);
    }

    public void updateRating(BigDecimal newRating, int reviewCount) {
        this.rating = newRating;
        this.totalReviews = reviewCount;
    }

    public void incrementTotalSold(int quantity) {
        this.totalSold += quantity;
    }
}