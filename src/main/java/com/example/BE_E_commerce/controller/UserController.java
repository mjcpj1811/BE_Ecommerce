package com.example.BE_E_commerce.controller;

import com.example.BE_E_commerce.dto.request.AddressRequest;
import com.example.BE_E_commerce.dto.response.AddressResponse;
import com.example.BE_E_commerce.dto.response.MessageResponse;
import com.example.BE_E_commerce.dto.response.UploadAvatarResponse;
import com.example.BE_E_commerce.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User profile and address management API")
public class UserController {

    private final UserService userService;

    // ========== AVATAR MANAGEMENT ==========

    /**
     * Upload avatar
     */
    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload avatar", description = "Upload user avatar image")
    public ResponseEntity<UploadAvatarResponse> uploadAvatar(
            @RequestParam("file") MultipartFile file) {
        UploadAvatarResponse response = userService.uploadAvatar(file);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete avatar
     */
    @DeleteMapping("/me/avatar")
    @Operation(summary = "Delete avatar", description = "Delete user avatar image")
    public ResponseEntity<MessageResponse> deleteAvatar() {
        userService.deleteAvatar();
        return ResponseEntity.ok(new MessageResponse("Avatar deleted successfully"));
    }

    // ========== ADDRESS MANAGEMENT ==========

    /**
     * Get all addresses
     */
    @GetMapping("/me/addresses")
    @Operation(summary = "Get all addresses", description = "Get all delivery addresses of current user")
    public ResponseEntity<List<AddressResponse>> getAllAddresses() {
        List<AddressResponse> addresses = userService. getAllAddresses();
        return ResponseEntity.ok(addresses);
    }

    /**
     * Get address by ID
     */
    @GetMapping("/me/addresses/{id}")
    @Operation(summary = "Get address by ID", description = "Get specific address by ID")
    public ResponseEntity<AddressResponse> getAddressById(@PathVariable Long id) {
        AddressResponse address = userService.getAddressById(id);
        return ResponseEntity.ok(address);
    }

    /**
     * Get default address
     */
    @GetMapping("/me/addresses/default")
    @Operation(summary = "Get default address", description = "Get default delivery address")
    public ResponseEntity<AddressResponse> getDefaultAddress() {
        AddressResponse address = userService. getDefaultAddress();
        return ResponseEntity.ok(address);
    }

    /**
     * Create new address
     */
    @PostMapping("/me/addresses")
    @Operation(summary = "Create address", description = "Create new delivery address")
    public ResponseEntity<AddressResponse> createAddress(@Valid @RequestBody AddressRequest request) {
        AddressResponse address = userService.createAddress(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(address);
    }

    /**
     * Update address
     */
    @PutMapping("/me/addresses/{id}")
    @Operation(summary = "Update address", description = "Update existing delivery address")
    public ResponseEntity<AddressResponse> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request) {
        AddressResponse address = userService.updateAddress(id, request);
        return ResponseEntity.ok(address);
    }

    /**
     * Set default address
     */
    @PutMapping("/me/addresses/{id}/default")
    @Operation(summary = "Set default address", description = "Set an address as default")
    public ResponseEntity<AddressResponse> setDefaultAddress(@PathVariable Long id) {
        AddressResponse address = userService. setDefaultAddress(id);
        return ResponseEntity.ok(address);
    }

    /**
     * Delete address
     */
    @DeleteMapping("/me/addresses/{id}")
    @Operation(summary = "Delete address", description = "Delete delivery address")
    public ResponseEntity<MessageResponse> deleteAddress(@PathVariable Long id) {
        userService.deleteAddress(id);
        return ResponseEntity.ok(new MessageResponse("Address deleted successfully"));
    }
}