package dev.safi.restaurant_management_system.dto.rbac;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Permission Override Response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Permission override details")
public class PermissionOverrideResponse {

    private Long id;
    private Long userId;
    private PermissionResponse permission;
    private String overrideType;
    private LocalDateTime expiresAt;
    private String reason;
    private String grantedByName;
    private LocalDateTime createdAt;
}
