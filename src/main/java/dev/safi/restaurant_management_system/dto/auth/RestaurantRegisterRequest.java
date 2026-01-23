package dev.safi.restaurant_management_system.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Restaurant Registration DTO (for Salesman onboarding)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Restaurant onboarding request")
public class RestaurantRegisterRequest {

    @NotBlank(message = "Restaurant name is required")
    @Size(min = 2, max = 100)
    @Schema(description = "Restaurant name", example = "Golden Fork Restaurant")
    private String restaurantName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Restaurant email", example = "contact@goldenfork.com")
    private String restaurantEmail;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number")
    @Schema(description = "Restaurant phone", example = "+8801712345678")
    private String restaurantPhone;

    @NotBlank(message = "Address is required")
    @Size(max = 500)
    @Schema(description = "Restaurant address", example = "123 Main Street, Dhaka")
    private String address;

    @NotBlank(message = "Admin name is required")
    @Schema(description = "Restaurant admin full name", example = "Jane Smith")
    private String adminName;

    @NotBlank(message = "Admin email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Admin email", example = "jane@goldenfork.com")
    private String adminEmail;

    @NotBlank(message = "Admin password is required")
    @Size(min = 8, max = 100)
    @Schema(description = "Admin password", example = "Admin@123")
    private String adminPassword;
}
