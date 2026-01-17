package com.example.BE_E_commerce.service;

import com.example.BE_E_commerce.dto.request.*;
import com.example.BE_E_commerce. dto.response.JwtResponse;
import com.example.BE_E_commerce. dto.response.MessageResponse;
import com.example.BE_E_commerce.dto.response.UserResponse;
import com.example.BE_E_commerce.entity.User;
import com.example.BE_E_commerce.exception.BadRequestException;
import com.example.BE_E_commerce.exception.DuplicateResourceException;
import com.example.BE_E_commerce.exception. ResourceNotFoundException;
import com.example.BE_E_commerce.exception.UnauthorizedException;
import com.example.BE_E_commerce.mapper.UserMapper;
import com.example.BE_E_commerce.repository.UserRepository;
import com.example.BE_E_commerce.security.JwtTokenProvider;
import com.example.BE_E_commerce.security.UserDetailsImpl;
import com.example.BE_E_commerce.service.RedisService;
import com.example.BE_E_commerce.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok. extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.example.BE_E_commerce.constant.RedisKeyConstants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;
    private final EmailService emailService; // ← THÊM

    /**
     * Register new user
     */
    @Transactional
    public JwtResponse register(RegisterRequest request) {
        log.info("Registering new user with username: {}", request.getUsername());

        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username is already taken");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already registered");
        }

        // Map DTO to Entity
        User user = userMapper.toEntity(request);

        // Hash password
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        // Save user
        user = userRepository.save(user);

        log.info("User registered successfully: {} (ID: {})", user.getUsername(), user.getId());

        //  Send welcome email
        emailService.sendWelcomeEmail(user. getEmail(), user.getFullName(), user.getUsername());

        // Auto login after registration
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate JWT token
        String jwt = jwtTokenProvider.generateToken(authentication);

        // Cache user session in Redis
        UserResponse userResponse = userMapper.toResponse(user);
        redisService.set(userSessionKey(user.getId()), userResponse, TTL_USER_SESSION, TimeUnit.SECONDS);

        return new JwtResponse(jwt, userResponse);
    }

    /**
     * Login user
     */
    @Transactional(readOnly = true)
    public JwtResponse login(LoginRequest request) {
        log.info("User attempting to login:  {}", request.getUsernameOrEmail());

        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder. getContext().setAuthentication(authentication);

        // Generate JWT token
        String jwt = jwtTokenProvider.generateToken(authentication);

        // Get user details
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Get user from database
        User user = userRepository. findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));

        UserResponse userResponse = userMapper.toResponse(user);

        // Cache user session in Redis
        redisService.set(userSessionKey(user.getId()), userResponse, TTL_USER_SESSION, TimeUnit.SECONDS);

        log.info("User logged in successfully: {} (ID: {})", user.getUsername(), user.getId());

        return new JwtResponse(jwt, userResponse);
    }

    /**
     * Logout user (blacklist token)
     */
    public MessageResponse logout(HttpServletRequest request) {
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));

        // Get token from request
        String token = getJwtFromRequest(request);

        if (StringUtils.hasText(token)) {
            // Calculate token remaining time
            Date expiration = jwtTokenProvider.getExpirationFromToken(token);
            if (expiration != null) {
                long remainingTime = expiration.getTime() - System.currentTimeMillis();

                if (remainingTime > 0) {
                    // Blacklist token until it expires
                    long ttlSeconds = TimeUnit.MILLISECONDS. toSeconds(remainingTime);
                    redisService.set(
                            tokenBlacklistKey(token),
                            "BLACKLISTED",
                            ttlSeconds,
                            TimeUnit.SECONDS
                    );

                    log.info("Token blacklisted for {} seconds", ttlSeconds);
                }
            }
        }

        // Remove user session from Redis
        redisService. delete(userSessionKey(userId));

        // Clear security context
        SecurityContextHolder. clearContext();

        log.info("User logged out successfully: {}", userId);

        return new MessageResponse("Logged out successfully");
    }

    /**
     * Extract JWT from request
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken. substring(7);
        }

        return null;
    }

    /**
     * Get current user profile
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));

        // Try to get from Redis cache first
        UserResponse cachedUser = redisService.get(userSessionKey(userId), UserResponse.class);
        if (cachedUser != null) {
            log.debug("User profile retrieved from cache: {}", userId);
            return cachedUser;
        }

        // Get from database
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        UserResponse userResponse = userMapper.toResponse(user);

        // Cache for future requests
        redisService.set(userSessionKey(userId), userResponse, TTL_USER_SESSION, TimeUnit.SECONDS);

        return userResponse;
    }

    /**
     * Update user profile
     */
    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request) {
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Update fields
        userMapper.updateEntityFromDto(request, user);

        // Save
        user = userRepository.save(user);

        UserResponse userResponse = userMapper.toResponse(user);

        // Update cache
        redisService.set(userSessionKey(userId), userResponse, TTL_USER_SESSION, TimeUnit.SECONDS);

        log.info("User profile updated: {}", userId);

        return userResponse;
    }

    /**
     * Change password
     */
    @Transactional
    public MessageResponse changePassword(ChangePasswordRequest request) {
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));

        // Validate confirm password
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request. getNewPassword()));
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", userId);

        return new MessageResponse("Password changed successfully");
    }

    /**
     * Forgot password (send reset link via email)
     */
    @Transactional(readOnly = true)
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        // Generate reset token
        String resetToken = jwtTokenProvider.generateTokenFromUsername(user.getUsername());

        // Store token in Redis with 15 minutes expiration
        redisService.set("reset_token:" + user.getId(), resetToken, 15, TimeUnit.MINUTES);

        //  Send email with reset link
        emailService. sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetToken);

        log.info("Password reset email sent to: {}", user.getEmail());

        return new MessageResponse("Password reset link sent to your email");
    }

    /**
     * Reset password
     */
    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        // Validate token
        if (!jwtTokenProvider.validateToken(request.getToken())) {
            throw new BadRequestException("Invalid or expired reset token");
        }

        // Validate confirm password
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }

        // Get username from token
        String username = jwtTokenProvider.getUsernameFromToken(request.getToken());

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify token in Redis
        String storedToken = redisService.get("reset_token:" + user. getId(), String.class);
        if (storedToken == null || !storedToken.equals(request.getToken())) {
            throw new BadRequestException("Invalid or expired reset token");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository. save(user);

        // Remove token from Redis
        redisService. delete("reset_token:" + user.getId());

        log.info("Password reset successfully for user: {}", username);

        return new MessageResponse("Password reset successfully");
    }

    /**
     * Check if username is available
     */
    @Transactional(readOnly = true)
    public MessageResponse checkUsername(String username) {
        boolean exists = userRepository.existsByUsername(username);
        if (exists) {
            throw new DuplicateResourceException("Username is already taken");
        }
        return new MessageResponse("Username is available");
    }

    /**
     * Check if email is available
     */
    @Transactional(readOnly = true)
    public MessageResponse checkEmail(String email) {
        boolean exists = userRepository.existsByEmail(email);
        if (exists) {
            throw new DuplicateResourceException("Email is already registered");
        }
        return new MessageResponse("Email is available");
    }
}