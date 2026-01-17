package com.example.BE_E_commerce.controller;

import com.example.BE_E_commerce.dto. request.*;
import com.example.BE_E_commerce. dto.response.JwtResponse;
import com.example.BE_E_commerce.dto.response.MessageResponse;
import com.example.BE_E_commerce.dto.response.UserResponse;
import com.example.BE_E_commerce.service. AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication API")
public class AuthController {

    private final AuthService authService;

    /**
     * Register new user
     */
    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Create a new user account")
    public ResponseEntity<JwtResponse> register(@Valid @RequestBody RegisterRequest request) {
        JwtResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login
     */
    @PostMapping("/login")
    @Operation(summary = "Login", description = "Login with username/email and password")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        JwtResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Logout (UPDATED - truyền HttpServletRequest)
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Logout current user")
    public ResponseEntity<MessageResponse> logout(HttpServletRequest request) {
        MessageResponse response = authService. logout(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get current user
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get current authenticated user profile")
    public ResponseEntity<UserResponse> getCurrentUser() {
        UserResponse response = authService.getCurrentUser();
        return ResponseEntity.ok(response);
    }

    /**
     * Update profile
     */
    @PutMapping("/me")
    @Operation(summary = "Update profile", description = "Update current user profile")
    public ResponseEntity<UserResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        UserResponse response = authService.updateProfile(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Change password
     */
    @PutMapping("/change-password")
    @Operation(summary = "Change password", description = "Change current user password")
    public ResponseEntity<MessageResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        MessageResponse response = authService.changePassword(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Forgot password
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Request password reset link")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        MessageResponse response = authService.forgotPassword(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Reset password
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset password with token")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        MessageResponse response = authService.resetPassword(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Check username availability
     */
    @GetMapping("/check-username")
    @Operation(summary = "Check username", description = "Check if username is available")
    public ResponseEntity<MessageResponse> checkUsername(@RequestParam String username) {
        MessageResponse response = authService.checkUsername(username);
        return ResponseEntity.ok(response);
    }

    /**
     * Check email availability
     */
    @GetMapping("/check-email")
    @Operation(summary = "Check email", description = "Check if email is available")
    public ResponseEntity<MessageResponse> checkEmail(@RequestParam String email) {
        MessageResponse response = authService.checkEmail(email);
        return ResponseEntity. ok(response);
    }
}