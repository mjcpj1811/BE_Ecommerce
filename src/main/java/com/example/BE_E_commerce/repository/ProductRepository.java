package com.example.BE_E_commerce.repository;

import com.example.BE_E_commerce.entity.Product;
import com.example.BE_E_commerce.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository  extends JpaRepository<Product,Long> {


    // Find by slug
    Optional<Product> findBySlug(String slug);

    // Check slug exists
    boolean existsBySlug(String slug);

    // Find by status
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    // Find by category
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    // Find by shop
    Page<Product> findByShopId(Long shopId, Pageable pageable);

    // Find active products by category
    Page<Product> findByCategoryIdAndStatus(Long categoryId, ProductStatus status, Pageable pageable);

    // Find active products by multiple categories (parent + subcategories)
    Page<Product> findByCategoryIdInAndStatus(List<Long> categoryIds, ProductStatus status, Pageable pageable);

    // Search by name (simple)
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND p.status = :status")
    Page<Product> searchByName(@Param("keyword") String keyword,
                               @Param("status") ProductStatus status,
                               Pageable pageable);

    // Advanced search with filters - for non-price sorting
    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN p.variants v " +
            "WHERE (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:categoryIds IS NULL OR p.category.id IN :categoryIds) " +
            "AND (:shopId IS NULL OR p.shop.id = :shopId) " +
            "AND (:minPrice IS NULL OR v.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR v.price <= :maxPrice) " +
            "AND p.status = :status")
    Page<Product> searchWithFilters(@Param("keyword") String keyword,
                                    @Param("categoryIds") List<Long> categoryIds,
                                    @Param("shopId") Long shopId,
                                    @Param("minPrice") BigDecimal minPrice,
                                    @Param("maxPrice") BigDecimal maxPrice,
                                    @Param("status") ProductStatus status,
                                    Pageable pageable);

    // Advanced search with price sorting (ASC)
    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN p.variants v " +
            "WHERE (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:categoryIds IS NULL OR p.category.id IN :categoryIds) " +
            "AND (:shopId IS NULL OR p.shop.id = :shopId) " +
            "AND (:minPrice IS NULL OR v.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR v.price <= :maxPrice) " +
            "AND p.status = :status " +
            "ORDER BY (SELECT MIN(v2.price) FROM ProductVariant v2 WHERE v2.product = p) ASC")
    Page<Product> searchWithFiltersOrderByPriceAsc(@Param("keyword") String keyword,
                                                     @Param("categoryIds") List<Long> categoryIds,
                                                     @Param("shopId") Long shopId,
                                                     @Param("minPrice") BigDecimal minPrice,
                                                     @Param("maxPrice") BigDecimal maxPrice,
                                                     @Param("status") ProductStatus status,
                                                     Pageable pageable);

    // Advanced search with price sorting (DESC)
    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN p.variants v " +
            "WHERE (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:categoryIds IS NULL OR p.category.id IN :categoryIds) " +
            "AND (:shopId IS NULL OR p.shop.id = :shopId) " +
            "AND (:minPrice IS NULL OR v.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR v.price <= :maxPrice) " +
            "AND p.status = :status " +
            "ORDER BY (SELECT MIN(v2.price) FROM ProductVariant v2 WHERE v2.product = p) DESC")
    Page<Product> searchWithFiltersOrderByPriceDesc(@Param("keyword") String keyword,
                                                      @Param("categoryIds") List<Long> categoryIds,
                                                      @Param("shopId") Long shopId,
                                                      @Param("minPrice") BigDecimal minPrice,
                                                      @Param("maxPrice") BigDecimal maxPrice,
                                                      @Param("status") ProductStatus status,
                                                      Pageable pageable);

    // Get best sellers (top sold products)
    @Query("SELECT p FROM Product p WHERE p.status = :status ORDER BY p.totalSold DESC")
    Page<Product> findBestSellers(@Param("status") ProductStatus status, Pageable pageable);

    // Get new arrivals
    @Query("SELECT p FROM Product p WHERE p.status = :status ORDER BY p.createdAt DESC")
    Page<Product> findNewArrivals(@Param("status") ProductStatus status, Pageable pageable);

    // Get top rated products
    @Query("SELECT p FROM Product p WHERE p.status = : status AND p.averageRating >= :minRating ORDER BY p. averageRating DESC")
    Page<Product> findTopRated(@Param("status") ProductStatus status,
                               @Param("minRating") Double minRating,
                               Pageable pageable);

    // Get related products (same category, exclude current)
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.id <> :productId AND p.status = :status")
    List<Product> findRelatedProducts(@Param("categoryId") Long categoryId,
                                      @Param("productId") Long productId,
                                      @Param("status") ProductStatus status,
                                      Pageable pageable);

    // Count products by shop
    long countByShopId(Long shopId);

    // Count products by category
    long countByCategoryId(Long categoryId);
}
