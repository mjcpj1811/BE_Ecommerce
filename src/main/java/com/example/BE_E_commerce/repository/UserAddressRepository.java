package com.example.BE_E_commerce.repository;

import com.example.BE_E_commerce.entity.UserAddress;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress,Long> {
    // Find all addresses by user ID
    List<UserAddress> findByUserIdOrderByIsDefaultDescCreatedAtDesc(Long userId);

    // Find default address by user ID
    Optional<UserAddress> findByUserIdAndIsDefaultTrue(Long userId);

    // Find address by ID and user ID (for security check)
    Optional<UserAddress> findByIdAndUserId(Long id, Long userId);

    // Count addresses by user ID
    long countByUserId(Long userId);

    // Set all addresses of a user to non-default
    @Modifying
    @Query("UPDATE UserAddress a SET a.isDefault = false WHERE a.user.id = :userId")
    void unsetDefaultForUser(@Param("userId") Long userId);

    // Check if address belongs to user
    boolean existsByIdAndUserId(Long id, Long userId);
}
