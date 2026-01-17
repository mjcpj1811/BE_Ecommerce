package com.example.BE_E_commerce.entity;


import com.example.BE_E_commerce.enums.VoucherType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java. math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vouchers", indexes = {
        @Index(name = "idx_code", columnList = "code"),
        @Index(name = "idx_shop_id", columnList = "shop_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType. IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop; // NULL = platform voucher

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoucherType type;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal discountValue;

    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal minOrderValue = BigDecimal. ZERO;

    @Column(precision = 15, scale = 2)
    private BigDecimal maxDiscountAmount;

    private Integer usageLimit;

    @Column(nullable = false)
    @Builder.Default
    private Integer usedCount = 0;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ========== HELPER METHODS ==========

    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return isActive
                && now.isAfter(startDate)
                && now.isBefore(endDate)
                && (usageLimit == null || usedCount < usageLimit);
    }

    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        if (orderAmount.compareTo(minOrderValue) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount;
        if (type == VoucherType.PERCENTAGE) {
            discount = orderAmount
                    .multiply(discountValue)
                    .divide(new BigDecimal("100"), 2, BigDecimal. ROUND_HALF_UP);
        } else {
            discount = discountValue;
        }

        if (maxDiscountAmount != null && discount. compareTo(maxDiscountAmount) > 0) {
            discount = maxDiscountAmount;
        }

        return discount;
    }

    public void incrementUsedCount() {
        this.usedCount++;
        if (usageLimit != null && usedCount >= usageLimit) {
            this.isActive = false;
        }
    }
}