package com.example.BE_E_commerce.repository;

import org.hibernate.type.descriptor.converter.spi.JpaAttributeConverter;
import org.springframework.stereotype.Repository;

import com.example.BE_E_commerce.entity.Shop;
import com.example.BE_E_commerce.enums.ShopStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long>,
        JpaSpecificationExecutor<Shop> {

    // Find by slug
    Optional<Shop> findBySlug(String slug);

    // Check slug exists
    boolean existsBySlug(String slug);

    // Find by owner
    Optional<Shop> findByOwnerId(Long ownerId);

    // Check if user has shop
    boolean existsByOwnerId(Long ownerId);

    // Find by status
    Page<Shop> findByStatus(ShopStatus status, Pageable pageable);

    // Find active shops
    Page<Shop> findByStatusOrderByCreatedAtDesc(ShopStatus status, Pageable pageable);

    // Search by name
    @Query("SELECT s FROM Shop s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND s.status = :status")
    Page<Shop> searchByName(@Param("keyword") String keyword,
                            @Param("status") ShopStatus status,
                            Pageable pageable);

    // Advanced search with filters
    @Query("SELECT s FROM Shop s " +
            "WHERE (:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:status IS NULL OR s.status = :status) " +
            "AND (:minRating IS NULL OR s.rating >= :minRating)")
    Page<Shop> searchWithFilters(@Param("keyword") String keyword,
                                 @Param("status") ShopStatus status,
                                 @Param("minRating") Double minRating,
                                 Pageable pageable);

    // Get top rated shops
    @Query("SELECT s FROM Shop s WHERE s.status = :status AND s.rating >= :minRating ORDER BY s.rating DESC")
    Page<Shop> findTopRated(@Param("status") ShopStatus status,
                            @Param("minRating") Double minRating,
                            Pageable pageable);

    // Get shops with most products
    @Query("SELECT s FROM Shop s WHERE s.status = :status ORDER BY s.totalProducts DESC")
    Page<Shop> findMostProducts(@Param("status") ShopStatus status, Pageable pageable);

    // Get best seller shops
    @Query("SELECT s FROM Shop s WHERE s.status = :status ORDER BY s.totalSold DESC")
    Page<Shop> findBestSellers(@Param("status") ShopStatus status, Pageable pageable);

    // Count active shops
    long countByStatus(ShopStatus status);
}