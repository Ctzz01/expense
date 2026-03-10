package com.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "applicationId is required")
    private String applicationId;

    @NotBlank(message = "identifier is required (email, phone, or userId)")
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;
}
