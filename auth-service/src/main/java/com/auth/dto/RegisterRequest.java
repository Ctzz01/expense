package com.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "applicationId is required")
    private String applicationId;

    @Email(message = "Invalid email format")
    private String email;

    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private String role;

    @AssertTrue(message = "At least one of email or phoneNumber is required")
    public boolean isEmailOrPhonePresent() {
        return (email != null && !email.trim().isEmpty()) || 
               (phoneNumber != null && !phoneNumber.trim().isEmpty());
    }
}
