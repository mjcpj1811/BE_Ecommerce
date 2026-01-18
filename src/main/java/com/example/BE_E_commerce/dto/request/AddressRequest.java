package com.example.BE_E_commerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddressRequest {

    @NotBlank(message = "Recipient name is required")
    @Size(min = 2, max = 255, message = "Recipient name must be between 2 and 255 characters")
    private String recipientName;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^(\\+84|0)[0-9]{9}$", message = "Invalid Vietnamese phone number")
    private String phone;

    @NotBlank(message = "Address line is required")
    @Size(min = 5, max = 500, message = "Address must be between 5 and 500 characters")
    private String addressLine;

    @NotBlank(message = "Ward is required")
    @Size(max = 100)
    private String ward;

    @NotBlank(message = "District is required")
    @Size(max = 100)
    private String district;

    @NotBlank(message = "City is required")
    @Size(max = 100)
    private String city;

    private Boolean isDefault;
}
