package dev.safi.restaurant_management_system.dto.rbac;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Custom Role Create Request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Create custom role request")
public class CustomRoleRequest {

    @NotBlank(message = "Role name is required")
    @Size(min = 3, max = 100)
    @Schema(description = "Role name", example = "kitchen_supervisor")
    private String name;

    @NotBlank(message = "Display name is required")
    @Size(min = 3, max = 100)
    @Schema(description = "Display name", example = "Kitchen Supervisor")
    private String displayName;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Base role ID to inherit permissions from")
    private Long basedOnRoleId;

    @NotNull(message = "Permissions are required")
    @Size(min = 1, message = "At least one permission is required")
    @Schema(description = "Permission IDs to assign")
    private Set<Long> permissionIds;
}
