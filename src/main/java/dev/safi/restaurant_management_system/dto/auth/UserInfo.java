package dev.safi.restaurant_management_system.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * User Info DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User information")
public class UserInfo {

    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "Full name", example = "John Doe")
    private String fullName;

    @Schema(description = "Email address", example = "john@example.com")
    private String email;

    @Schema(description = "Phone number", example = "+8801712345678")
    private String phoneNumber;

    @Schema(description = "Restaurant ID", example = "5")
    private Long restaurantId;

    @Schema(description = "Restaurant name", example = "Golden Fork")
    private String restaurantName;

    @Schema(description = "User roles", example = "[\"ROLE_CUSTOMER\"]")
    private Set<String> roles;

    @Schema(description = "Email verification status", example = "true")
    private Boolean isEmailVerified;

    @Schema(description = "Phone verification status", example = "false")
    private Boolean isPhoneVerified;

    @Schema(description = "Last login time")
    private LocalDateTime lastLogin;
}