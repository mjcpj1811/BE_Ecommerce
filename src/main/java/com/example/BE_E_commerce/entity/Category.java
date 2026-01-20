package com.example.BE_E_commerce. entity;

import jakarta.persistence.*;
import lombok.*;
import org. hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories", indexes = {
        @Index(name = "idx_parent_id", columnList = "parent_id"),
        @Index(name = "idx_slug", columnList = "slug"),
        @Index(name = "idx_level", columnList = "level"),
        @Index(name = "idx_display_order", columnList = "display_order")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true, nullable = false, length = 255)
    private String slug;

    // ← THÊM:  Description
    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    // ← THÊM: Level (0=root, 1=child, 2=grandchild, etc.)
    @Column(nullable = false)
    @Builder.Default
    private Integer level = 0;

    // ← ĐỔI TÊN:  iconUrl → imageUrl (hoặc giữ iconUrl và update DTO/Service)
    @Column(length = 500)
    private String imageUrl;  // Thay vì iconUrl

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ← THÊM: UpdatedAt
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ========== RELATIONSHIPS ==========

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Category> subCategories = new ArrayList<>();

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Product> products = new ArrayList<>();

    // ========== HELPER METHODS ==========

    /**
     * Add subcategory
     */
    public void addSubCategory(Category subCategory) {
        subCategories.add(subCategory);
        subCategory.setParent(this);
        subCategory.setLevel(this.level + 1);
    }

    /**
     * Remove subcategory
     */
    public void removeSubCategory(Category subCategory) {
        subCategories.remove(subCategory);
        subCategory.setParent(null);
    }
}