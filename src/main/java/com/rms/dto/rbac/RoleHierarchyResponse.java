package com.rms.dto.rbac;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Role Hierarchy Response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Role hierarchy structure")
public class RoleHierarchyResponse {

    @Schema(description = "Role name")
    private String roleName;

    @Schema(description = "Role level")
    private Integer roleLevel;

    @Schema(description = "Can manage roles below this level")
    private List<String> managableRoles;

    @Schema(description = "Total permissions count")
    private Integer permissionCount;
}
