package dev.safi.restaurant_management_system.dto.rbac;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Permission Category Response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Permissions grouped by category")
public class PermissionCategoryResponse {

    @Schema(description = "Category name", example = "MENU_MANAGEMENT")
    private String category;

    @Schema(description = "Category display name", example = "Menu Management")
    private String displayName;

    @Schema(description = "Permissions in this category")
    private List<PermissionResponse> permissions;
}
