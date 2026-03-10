package com.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    
    @NotBlank(message = "applicationId is required")
    private String applicationId;
    
    @NotBlank(message = "identifier is required (email or phone number)")
    private String identifier;
    
    @NotBlank(message = "new password is required")
    @Size(min = 6, message = "password must be at least 6 characters long")
    private String newPassword;
}
