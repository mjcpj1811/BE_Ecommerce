package com.example.BE_E_commerce.repository;

import com.example.BE_E_commerce.entity.Review;
import com.example.BE_E_commerce.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review,Long> {
    // Find by product
    Page<Review> findByProductId(Long productId, Pageable pageable);

    // Find approved reviews by product
    Page<Review> findByProductIdAndStatus(Long productId, ReviewStatus status, Pageable pageable);

    // Get recent reviews
    List<Review> findTop5ByProductIdAndStatusOrderByCreatedAtDesc(Long productId, ReviewStatus status);

    // Calculate average rating
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId AND r.status = :status")
    Double calculateAverageRating(@Param("productId") Long productId, @Param("status") ReviewStatus status);

    // Count reviews
    long countByProductIdAndStatus(Long productId, ReviewStatus status);
}
