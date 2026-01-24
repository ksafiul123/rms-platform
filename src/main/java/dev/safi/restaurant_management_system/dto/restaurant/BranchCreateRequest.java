package dev.safi.restaurant_management_system.dto.restaurant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Restaurant Branch Creation Request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Restaurant branch creation request")
public class BranchCreateRequest {

    @NotBlank(message = "Branch name is required")
    @Size(min = 2, max = 100)
    @Schema(description = "Branch name", example = "Downtown Branch")
    private String branchName;

    @Email(message = "Invalid email format")
    @Schema(description = "Branch contact email", example = "downtown@goldenfork.com")
    private String contactEmail;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number")
    @Schema(description = "Branch phone number", example = "+8801712345678")
    private String contactPhone;

    @NotBlank(message = "Address is required")
    @Size(max = 500)
    @Schema(description = "Full address", example = "123 Main Street, Gulshan 2")
    private String address;

    @Schema(description = "City", example = "Dhaka")
    private String city;

    @Schema(description = "ZIP code", example = "1212")
    private String zipCode;

    @Schema(description = "State/Division", example = "Dhaka Division")
    private String state;

    @Schema(description = "Country", example = "Bangladesh")
    private String country;

    @Schema(description = "Latitude", example = "23.7808875")
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private BigDecimal latitude;

    @Schema(description = "Longitude", example = "90.4218288")
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private BigDecimal longitude;

    @Schema(description = "Opening time", example = "09:00")
    private String openingTime;

    @Schema(description = "Closing time", example = "22:00")
    private String closingTime;

    @Schema(description = "Is main branch", example = "false")
    private Boolean isMainBranch;
}
