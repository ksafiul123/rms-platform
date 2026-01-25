package com.rms.dto.rbac;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Permission Override Request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Grant/revoke permission override")
public class PermissionOverrideRequest {

    @NotNull(message = "User ID is required")
    @Schema(description = "User ID", example = "10")
    private Long userId;

    @NotNull(message = "Permission ID is required")
    @Schema(description = "Permission ID", example = "25")
    private Long permissionId;

    @NotBlank(message = "Override type is required")
    @Schema(description = "Override type", example = "GRANT")
    private String overrideType;

    @Schema(description = "Expiration date (optional)")
    private LocalDateTime expiresAt;

    @NotBlank(message = "Reason is required")
    @Schema(description = "Reason for override")
    private String reason;
}
