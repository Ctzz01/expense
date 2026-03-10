package com.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequest {
    
    @NotBlank(message = "applicationId is required")
    private String applicationId;
    
    @NotBlank(message = "identifier is required (email or phone number)")
    private String identifier;
}
