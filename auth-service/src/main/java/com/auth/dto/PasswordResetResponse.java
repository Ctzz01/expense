package com.auth.dto;

import lombok.Data;

@Data
public class PasswordResetResponse {
    private String message;
    private boolean success;
    private String resetToken; // Only for forgot password response
    
    public static PasswordResetResponse success(String message, String resetToken) {
        PasswordResetResponse response = new PasswordResetResponse();
        response.setMessage(message);
        response.setSuccess(true);
        response.setResetToken(resetToken);
        return response;
    }
    
    public static PasswordResetResponse success(String message) {
        PasswordResetResponse response = new PasswordResetResponse();
        response.setMessage(message);
        response.setSuccess(true);
        return response;
    }
    
    public static PasswordResetResponse failure(String message) {
        PasswordResetResponse response = new PasswordResetResponse();
        response.setMessage(message);
        response.setSuccess(false);
        return response;
    }
}
