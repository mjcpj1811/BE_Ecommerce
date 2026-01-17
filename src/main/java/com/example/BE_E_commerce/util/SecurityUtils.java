package com.example.BE_E_commerce.util;

import com.example.BE_E_commerce.security.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public class SecurityUtils {

    private SecurityUtils() {
        // Prevent instantiation
    }

    /**
     * Get current authentication
     */
    public static Optional<Authentication> getCurrentAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String)) {
            return Optional.of(authentication);
        }
        return Optional.empty();
    }

    /**
     * Get current user details
     */
    public static Optional<UserDetailsImpl> getCurrentUser() {
        return getCurrentAuthentication()
                .map(auth -> (UserDetailsImpl) auth.getPrincipal());
    }

    /**
     * Get current user ID
     */
    public static Optional<Long> getCurrentUserId() {
        return getCurrentUser()
                .map(UserDetailsImpl::getId);
    }

    /**
     * Get current user email
     */
    public static Optional<String> getCurrentUserEmail() {
        return getCurrentUser()
                .map(UserDetailsImpl::getEmail);
    }

    /**
     * Get current username
     */
    public  static Optional<String> getCurrentUserName() {
        return getCurrentUser()
                .map(UserDetailsImpl::getUsername);
    }
    /**
     * Check if user is authenticated
     */
    public static boolean isAuthenticated() {
        return getCurrentAuthentication().isPresent();
    }

    /**
     * Check if current user has role
     */
    public static boolean hasRole(String role) {
        return getCurrentAuthentication()
                .map(auth -> auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority ->
                                grantedAuthority.getAuthority().equals("ROLE_" + role)))
                .orElse(false);
    }

    /**
     * Check if current user is admin
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Check if current user is seller
     */
    public static boolean isSeller() {
        return hasRole("SELLER");
    }

    /**
     * Check if current user is buyer
     */
    public static boolean isBuyer() {
        return hasRole("BUYER");
    }
}
