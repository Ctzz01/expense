package com.auth.controller;

import com.auth.dto.AuthResponse;
import com.auth.dto.ForgotPasswordRequest;
import com.auth.dto.LoginRequest;
import com.auth.dto.PasswordResetResponse;
import com.auth.dto.RegisterRequest;
import com.auth.dto.ResetPasswordRequest;
import com.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Register and login endpoints. Supports login by email, phone number, or userId.")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new user", description = "Creates a new user account. Requires applicationId and at least one of email or phoneNumber.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User registered successfully", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error or missing email/phone"),
        @ApiResponse(responseCode = "409", description = "Email or phone already registered for this application")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /auth/register - email={}", request.getEmail());
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(summary = "Login", description = "Authenticate using applicationId and any identifier: email, phone number, or userId. Returns a JWT token.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /auth/login - appId={}, identifier={}", request.getApplicationId(), request.getIdentifier());
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Health check", description = "Returns auth-service status.")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("auth-service is running");
    }

    @Operation(summary = "Forgot password", description = "Verify user exists for password reset using email or phone number.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User verified successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<PasswordResetResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("POST /auth/forgot-password - appId={}, identifier={}", request.getApplicationId(), request.getIdentifier());
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @Operation(summary = "Reset password", description = "Reset password directly using email or phone number and new password.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Password reset successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Invalid user credentials")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<PasswordResetResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("POST /auth/reset-password - appId={}, identifier={}", request.getApplicationId(), request.getIdentifier());
        return ResponseEntity.ok(authService.resetPassword(request));
    }
}
