package com.rms.controller;

import com.rms.dto.auth.ApiResponse;
import com.rms.dto.auth.AuthResponse;
import com.rms.dto.auth.LoginRequest;
import com.rms.dto.auth.RegisterRequest;
import com.rms.dto.auth.RestaurantInfo;
import com.rms.dto.auth.RestaurantRegisterRequest;
import com.rms.dto.auth.TokenRefreshRequest;
import com.rms.dto.auth.TokenRefreshResponse;
import com.rms.dto.auth.UserInfo;
import com.rms.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * Handles user authentication, registration, and token management
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and user management endpoints")
public class AuthController {

    private final AuthService authService;

    /**
     * User login endpoint
     */
    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "Authenticate user and return JWT tokens"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials"
            )
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Customer registration endpoint
     */
    @PostMapping("/register")
    @Operation(
            summary = "Customer registration",
            description = "Register a new customer account"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Registration successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Email or phone already exists"
            )
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Restaurant onboarding endpoint (Salesman only)
     */
    @PostMapping("/register-restaurant")
    @PreAuthorize("hasRole('SALESMAN') or hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Register new restaurant",
            description = "Onboard a new restaurant with admin account (Salesman/Super Admin only)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Restaurant registered successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Restaurant email already exists"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            )
    })
    public ResponseEntity<ApiResponse<RestaurantInfo>> registerRestaurant(
            @Valid @RequestBody RestaurantRegisterRequest request) {
        ApiResponse<RestaurantInfo> response = authService.registerRestaurant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Refresh token endpoint
     */
    @PostMapping("/refresh-token")
    @Operation(
            summary = "Refresh access token",
            description = "Get new access token using refresh token"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Token refreshed successfully",
                    content = @Content(schema = @Schema(implementation = TokenRefreshResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid or expired refresh token"
            )
    })
    public ResponseEntity<TokenRefreshResponse> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request) {
        TokenRefreshResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Logout endpoint
     */
    @PostMapping("/logout")
    @Operation(
            summary = "User logout",
            description = "Revoke refresh token and clear session"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Logout successful"
            )
    })
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestBody(required = false) TokenRefreshRequest request) {
        String refreshToken = request != null ? request.getRefreshToken() : null;
        ApiResponse<Void> response = authService.logout(refreshToken);
        return ResponseEntity.ok(response);
    }

    /**
     * Get current user profile
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get current user",
            description = "Retrieve authenticated user's profile information"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User profile retrieved",
                    content = @Content(schema = @Schema(implementation = UserInfo.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    public ResponseEntity<ApiResponse<UserInfo>> getCurrentUser() {
        // Implementation would fetch user from SecurityContext
        return ResponseEntity.ok(ApiResponse.success("User fetched successfully", null));
    }

    /**
     * Health check endpoint (public)
     */
    @GetMapping("/health")
    @Operation(
            summary = "Health check",
            description = "Check if authentication service is running"
    )
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(
                ApiResponse.success("Authentication service is running", "OK")
        );
    }
}
