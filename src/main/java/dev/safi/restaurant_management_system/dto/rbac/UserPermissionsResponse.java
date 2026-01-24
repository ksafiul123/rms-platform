package dev.safi.restaurant_management_system.dto.rbac;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * User Permissions Response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "User's complete permissions")
public class UserPermissionsResponse {

    @Schema(description = "User ID")
    private Long userId;

    @Schema(description = "User name")
    private String userName;

    @Schema(description = "Restaurant ID")
    private Long restaurantId;

    @Schema(description = "System roles")
    private List<String> systemRoles;

    @Schema(description = "Custom roles")
    private List<CustomRoleResponse> customRoles;

    @Schema(description = "All effective permissions")
    private List<PermissionResponse> permissions;

    @Schema(description = "Permission overrides")
    private List<PermissionOverrideResponse> overrides;
}
