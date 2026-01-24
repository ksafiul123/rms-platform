package dev.safi.restaurant_management_system.dto.rbac;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Permission Check Request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Check if user has permission")
public class PermissionCheckRequest {

    @NotNull(message = "User ID is required")
    @Schema(description = "User ID", example = "5")
    private Long userId;

    @NotBlank(message = "Permission name is required")
    @Schema(description = "Permission name", example = "menu:create")
    private String permissionName;

    @Schema(description = "Restaurant ID (for multi-tenant check)")
    private Long restaurantId;
}
