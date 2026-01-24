package dev.safi.restaurant_management_system.dto.rbac;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Assign Role Request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Assign role to user request")
public class AssignRoleRequest {

    @NotNull(message = "User ID is required")
    @Schema(description = "User ID", example = "5")
    private Long userId;

    @Schema(description = "System role name", example = "ROLE_CHEF")
    private String roleName;

    @Schema(description = "Custom role ID (for restaurant-specific roles)")
    private Long customRoleId;

    @Schema(description = "Reason for assignment")
    private String reason;
}
