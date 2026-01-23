package dev.safi.restaurant_management_system.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token Refresh Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Token refresh response")
public class TokenRefreshResponse {

    @Schema(description = "New access token", example = "eyJhbGciOiJIUzI1NiIs...")
    private String accessToken;

    @Schema(description = "New refresh token", example = "eyJhbGciOiJIUzI1NiIs...")
    private String refreshToken;

    @Schema(description = "Token type", example = "Bearer")
    private String tokenType = "Bearer";
}