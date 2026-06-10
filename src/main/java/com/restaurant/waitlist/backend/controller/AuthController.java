package com.restaurant.waitlist.backend.controller;

import com.restaurant.waitlist.backend.dto.request.ForgotPasswordRequest;
import com.restaurant.waitlist.backend.dto.request.LoginRequest;
import com.restaurant.waitlist.backend.dto.request.ResetPasswordRequest;
import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.dto.response.LoginResponse;
import com.restaurant.waitlist.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success("Login successful", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            authService.requestPasswordReset(request.getEmail());
            return ResponseEntity.ok(ApiResponse.success("Password reset email sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request.getToken(), request.getNewPassword(), request.getConfirmPassword());
            return ResponseEntity.ok(ApiResponse.success("Password reset successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * TEMPORARY DEV ENDPOINT - Generate BCrypt hash for a password.
     * Remove this endpoint in production!
     * Usage: POST /api/auth/encode-password?password=123456
     */
    @PostMapping("/encode-password")
    public ResponseEntity<ApiResponse<String>> encodePassword(@RequestParam String password) {
        // WARNING: This is for development/testing only. Remove in production.
        String encoded = passwordEncoder.encode(password);
        return ResponseEntity.ok(ApiResponse.success("Encoded password (use in INSERT script)", encoded));
    }
}

