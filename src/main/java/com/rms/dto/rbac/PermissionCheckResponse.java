package com.rms.dto.rbac;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Permission Check Response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Permission check result")
public class PermissionCheckResponse {

    @Schema(description = "Has permission")
    private Boolean hasPermission;

    @Schema(description = "Granted through (role/override)")
    private String grantedThrough;

    @Schema(description = "Details")
    private String details;
}
