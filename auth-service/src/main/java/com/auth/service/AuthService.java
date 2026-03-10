package com.auth.service;

import com.auth.dto.AuthResponse;
import com.auth.dto.ForgotPasswordRequest;
import com.auth.dto.LoginRequest;
import com.auth.dto.PasswordResetResponse;
import com.auth.dto.RegisterRequest;
import com.auth.dto.ResetPasswordRequest;
import com.auth.model.User;
import com.auth.repository.UserRepository;
import com.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        String appId = request.getApplicationId();
        log.info("Registering user for appId={}, email={}, phone={}", appId, request.getEmail(), request.getPhoneNumber());

        if (!StringUtils.hasText(request.getEmail()) && !StringUtils.hasText(request.getPhoneNumber())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one of email or phoneNumber is required");
        }

        if (StringUtils.hasText(request.getEmail()) && userRepository.existsByEmailAndApplicationId(request.getEmail(), appId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered for this application");
        }

        if (StringUtils.hasText(request.getPhoneNumber()) && userRepository.existsByPhoneNumberAndApplicationId(request.getPhoneNumber(), appId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone number already registered for this application");
        }

        User.Role role = User.Role.USER;
        if (StringUtils.hasText(request.getRole())) {
            try {
                role = User.Role.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role. Accepted values: USER, ADMIN");
            }
        }

        User user = User.builder()
                .applicationId(appId)
                .email(StringUtils.hasText(request.getEmail()) ? request.getEmail().toLowerCase() : null)
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        User saved = userRepository.save(user);
        log.info("User registered successfully id={} for appId={}", saved.getId(), appId);

        String token = jwtUtil.generateToken(saved.getId(), appId, saved.getEmail(), saved.getPhoneNumber(), saved.getRole().name());
        return new AuthResponse(token, saved.getId(), appId, saved.getEmail(), saved.getPhoneNumber(), saved.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        String appId = request.getApplicationId();
        String identifier = request.getIdentifier().trim();
        log.info("Login attempt for appId={}, identifier={}", appId, identifier);

        Optional<User> userOpt = Optional.empty();

        // 1. Try by email
        userOpt = userRepository.findByEmailAndApplicationId(identifier.toLowerCase(), appId);

        // 2. Try by phone number
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByPhoneNumberAndApplicationId(identifier, appId);
        }

        // 3. Try by userId (numeric)
        if (userOpt.isEmpty()) {
            try {
                Long userId = Long.parseLong(identifier);
                userOpt = userRepository.findByIdAndApplicationId(userId, appId);
            } catch (NumberFormatException ignored) {
            }
        }

        User user = userOpt.orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        log.info("Login successful for userId={}, appId={}", user.getId(), appId);
        String token = jwtUtil.generateToken(user.getId(), appId, user.getEmail(), user.getPhoneNumber(), user.getRole().name());
        return new AuthResponse(token, user.getId(), appId, user.getEmail(), user.getPhoneNumber(), user.getRole().name());
    }
    
    public PasswordResetResponse forgotPassword(ForgotPasswordRequest request) {
        String appId = request.getApplicationId();
        String identifier = request.getIdentifier().trim();
        log.info("Password reset request for appId={}, identifier={}", appId, identifier);
        
        Optional<User> userOpt = Optional.empty();
        
        // Try by email
        if (identifier.contains("@")) {
            userOpt = userRepository.findByEmailAndApplicationId(identifier.toLowerCase(), appId);
        } else {
            // Try by phone number
            userOpt = userRepository.findByPhoneNumberAndApplicationId(identifier, appId);
        }
        
        if (userOpt.isEmpty()) {
            // Don't reveal if user exists or not for security
            log.warn("Password reset requested for non-existent user: {} in app: {}", identifier, appId);
            return PasswordResetResponse.success("User verified. You can now reset your password.");
        }
        
        User user = userOpt.get();
        log.info("User verified for password reset: {} in app: {}", user.getId(), appId);
        
        // Always return success message (security by obscurity)
        return PasswordResetResponse.success("User verified. You can now reset your password.");
    }
    
    public PasswordResetResponse resetPassword(ResetPasswordRequest request) {
        String appId = request.getApplicationId();
        String identifier = request.getIdentifier().trim();
        String newPassword = request.getNewPassword();
        
        log.info("Password reset attempt for appId={}, identifier={}", appId, identifier);
        
        try {
            // Find user by email or phone
            Optional<User> userOpt = Optional.empty();
            
            if (identifier.contains("@")) {
                userOpt = userRepository.findByEmailAndApplicationId(identifier.toLowerCase(), appId);
            } else {
                userOpt = userRepository.findByPhoneNumberAndApplicationId(identifier, appId);
            }
            
            if (userOpt.isEmpty()) {
                log.warn("Password reset attempted for non-existent user: {} in app: {}", identifier, appId);
                return PasswordResetResponse.failure("Invalid user credentials");
            }
            
            User user = userOpt.get();
            
            // Update password directly
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            
            log.info("Password reset successful for user: {} in app: {}", user.getId(), appId);
            
            return PasswordResetResponse.success("Password has been reset successfully");
            
        } catch (Exception e) {
            log.error("Error during password reset", e);
            return PasswordResetResponse.failure("Failed to reset password. Please try again.");
        }
    }
}
