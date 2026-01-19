package com.example.BE_E_commerce.repository;

import com.example.BE_E_commerce.entity.User;
import com.example.BE_E_commerce.enums.AuthProvider;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.username = :identifier OR u.email = :identifier")
    Optional<User> findByUsernameOrEmail(String identifier);

     Boolean existsByUsername(String username);

     Boolean existsByEmail(String email);


    // Find by provider and provider ID
    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);

    // Check if email exists with different provider
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.provider != :provider")
    Optional<User> findByEmailAndDifferentProvider(@Param("email") String email, @Param("provider") AuthProvider provider);
}
