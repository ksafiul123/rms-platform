package dev.safi.restaurant_management_system.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token Refresh Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Token refresh request")
public class TokenRefreshRequest {

    @NotBlank(message = "Refresh token is required")
    @Schema(description = "Valid refresh token", example = "eyJhbGciOiJIUzI1NiIs...")
    private String refreshToken;
}