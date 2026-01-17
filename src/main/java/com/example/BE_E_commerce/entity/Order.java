package com.example.BE_E_commerce.entity;

import com.example.BE_E_commerce.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate. annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_shop_id", columnList = "shop_id"),
        @Index(name = "idx_order_code", columnList = "order_code"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String orderCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    // ========== DELIVERY ADDRESS ==========

    @Column(length = 255)
    private String recipientName;

    @Column(length = 20)
    private String phone;

    @Column(length = 500)
    private String addressLine;

    @Column(length = 100)
    private String ward;

    @Column(length = 100)
    private String district;

    @Column(length = 100)
    private String city;

    // ========== PRICING ==========

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal subtotal;

    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal. ZERO;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    // ========== COMMISSION ==========

    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal commissionRate;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal commissionAmount;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal sellerAmount;

    // ========== PAYMENT ==========

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus. PENDING;

    private LocalDateTime paidAt;

    // ========== STATUS ==========

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    // ========== NOTES ==========

    @Column(columnDefinition = "TEXT")
    private String buyerNote;

    @Column(columnDefinition = "TEXT")
    private String cancelReason;

    @Enumerated(EnumType.STRING)
    private CancelledBy cancelledBy;

    private LocalDateTime cancelledAt;
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version; // Optimistic locking

    // ========== RELATIONSHIPS ==========

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private RefundRequest refundRequest;

    // ========== HELPER METHODS ==========

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public boolean canBeCancelled() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }

    public boolean canBeReviewed() {
        return status == OrderStatus.COMPLETED;
    }

    public boolean isDelivered() {
        return status == OrderStatus.DELIVERED || status == OrderStatus.COMPLETED;
    }

    public void calculateTotals() {
        this.subtotal = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalAmount = subtotal
                .add(shippingFee != null ? shippingFee :  BigDecimal.ZERO)
                .subtract(discountAmount != null ? discountAmount : BigDecimal. ZERO);

        this. commissionAmount = totalAmount
                .multiply(commissionRate)
                .divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);

        this.sellerAmount = totalAmount. subtract(commissionAmount);
    }
}