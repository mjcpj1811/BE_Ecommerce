package com.example.BE_E_commerce.service;

import com.example.BE_E_commerce.dto.request.AddressRequest;
import com.example.BE_E_commerce.dto.response.AddressResponse;
import com.example.BE_E_commerce.dto.response.UploadAvatarResponse;
import com.example.BE_E_commerce.dto.response.UserResponse;
import com.example.BE_E_commerce.entity.User;
import com.example.BE_E_commerce.entity.UserAddress;
import com.example.BE_E_commerce.exception.BadRequestException;
import com.example.BE_E_commerce.exception.ResourceNotFoundException;
import com.example.BE_E_commerce.exception.UnauthorizedException;
import com.example.BE_E_commerce.mapper. AddressMapper;
import com.example.BE_E_commerce.mapper.UserMapper;
import com.example.BE_E_commerce.repository.UserAddressRepository;
import com.example.BE_E_commerce.repository.UserRepository;
import com.example.BE_E_commerce.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j. Slf4j;
import org. springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.example.BE_E_commerce.constant.RedisKeyConstants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserAddressRepository addressRepository;
    private final UserMapper userMapper;
    private final AddressMapper addressMapper;
    private final FileUploadService fileUploadService;
    private final RedisService redisService;

    // ========== AVATAR MANAGEMENT ==========

    /**
     * Upload avatar
     */
    @Transactional
    public UploadAvatarResponse uploadAvatar(MultipartFile file) {
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Delete old avatar if exists
        if (user.getAvatarUrl() != null) {
            String oldPublicId = fileUploadService. extractPublicId(user.getAvatarUrl());
            fileUploadService.deleteAvatar(oldPublicId);
        }

        // Upload new avatar
        Map<String, Object> uploadResult = fileUploadService. uploadAvatar(file, userId);

        // Update user avatar URL
        String avatarUrl = (String) uploadResult.get("secure_url");
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);

        // Update cache
        UserResponse userResponse = userMapper.toResponse(user);
        redisService.set(userSessionKey(userId), userResponse, TTL_USER_SESSION, TimeUnit.SECONDS);

        log.info("Avatar updated for user: {}", userId);

        return UploadAvatarResponse.builder()
                .avatarUrl(avatarUrl)
                .publicId((String) uploadResult.get("public_id"))
                .format((String) uploadResult.get("format"))
                .size(((Number) uploadResult.get("bytes")).longValue())
                .width((Integer) uploadResult.get("width"))
                .height((Integer) uploadResult.get("height"))
                .build();
    }

    /**
     * Delete avatar
     */
    @Transactional
    public void deleteAvatar() {
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getAvatarUrl() == null) {
            throw new BadRequestException("No avatar to delete");
        }

        // Delete from Cloudinary
        String publicId = fileUploadService. extractPublicId(user.getAvatarUrl());
        fileUploadService.deleteAvatar(publicId);

        // Update user
        user.setAvatarUrl(null);
        userRepository.save(user);

        // Update cache
        UserResponse userResponse = userMapper.toResponse(user);
        redisService.set(userSessionKey(userId), userResponse, TTL_USER_SESSION, TimeUnit.SECONDS);

        log.info("Avatar deleted for user: {}", userId);
    }

    // ========== ADDRESS MANAGEMENT ==========

    /**
     * Get all addresses of current user
     */
    @Transactional(readOnly = true)
    public List<AddressResponse> getAllAddresses() {
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));

        List<UserAddress> addresses = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);

        return addressMapper.toResponseList(addresses);
    }

    /**
     * Get address by ID
     */
    @Transactional(readOnly = true)
    public AddressResponse getAddressById(Long addressId) {
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));

        UserAddress address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        return addressMapper. toResponse(address);
    }

    /**
     * Get default address
     */
    @Transactional(readOnly = true)
    public AddressResponse getDefaultAddress() {
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));

        UserAddress address = addressRepository.findByUserIdAndIsDefaultTrue(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No default address found"));

        return addressMapper.toResponse(address);
    }

    /**
     * Create new address
     */
    @Transactional
    public AddressResponse createAddress(AddressRequest request) {
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Map DTO to entity
        UserAddress address = addressMapper.toEntity(request);
        address.setUser(user);

        // If this is the first address, set as default
        long addressCount = addressRepository.countByUserId(userId);
        if (addressCount == 0) {
            address.setIsDefault(true);
        }

        // Save
        address = addressRepository.save(address);

        log.info("Address created for user: {} - Address ID: {}", userId, address.getId());

        return addressMapper.toResponse(address);
    }

    /**
     * Update address
     */
    @Transactional
    public AddressResponse updateAddress(Long addressId, AddressRequest request) {
        Long userId = SecurityUtils. getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));

        UserAddress address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        // Update fields
        addressMapper.updateEntityFromDto(request, address);

        // Save
        address = addressRepository.save(address);

        log.info("Address updated: {} for user: {}", addressId, userId);

        return addressMapper.toResponse(address);
    }

    /**
     * Set address as default
     */
    @Transactional
    public AddressResponse setDefaultAddress(Long addressId) {
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));

        // Check if address exists and belongs to user
        UserAddress address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        // Unset all default addresses for this user
        addressRepository.unsetDefaultForUser(userId);

        // Set this address as default
        address.setIsDefault(true);
        address = addressRepository.save(address);

        log.info("Default address set: {} for user:  {}", addressId, userId);

        return addressMapper.toResponse(address);
    }

    /**
     * Delete address
     */
    @Transactional
    public void deleteAddress(Long addressId) {
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));

        UserAddress address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        boolean wasDefault = address.getIsDefault();

        // Delete address
        addressRepository.delete(address);

        // If deleted address was default, set another address as default
        if (wasDefault) {
            List<UserAddress> remainingAddresses = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
            if (!remainingAddresses.isEmpty()) {
                UserAddress newDefault = remainingAddresses.get(0);
                newDefault. setIsDefault(true);
                addressRepository.save(newDefault);
                log.info("New default address set: {} for user:  {}", newDefault.getId(), userId);
            }
        }

        log.info("Address deleted: {} for user: {}", addressId, userId);
    }
}