package com.rms.dto.rbac;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Role Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Role details with permissions")
public class RoleResponse {

    @Schema(description = "Role ID", example = "1")
    private Long id;

    @Schema(description = "Role name", example = "ROLE_RESTAURANT_ADMIN")
    private String name;

    @Schema(description = "Display name", example = "Restaurant Administrator")
    private String displayName;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Role hierarchy level", example = "3")
    private Integer roleLevel;

    @Schema(description = "Permissions assigned to role")
    private List<PermissionResponse> permissions;

    @Schema(description = "Is system role")
    private Boolean isSystem;

    @Schema(description = "Is active")
    private Boolean isActive;

    @Schema(description = "Created at")
    private LocalDateTime createdAt;
}
