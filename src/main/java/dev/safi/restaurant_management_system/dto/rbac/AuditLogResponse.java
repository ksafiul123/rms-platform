package dev.safi.restaurant_management_system.dto.rbac;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Audit Log Response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Permission audit log entry")
public class AuditLogResponse {

    private Long id;
    private Long userId;
    private String userName;
    private Long restaurantId;
    private String action;
    private String resource;
    private String endpoint;
    private String permissionName;
    private Boolean accessGranted;
    private String ipAddress;
    private String details;
    private LocalDateTime timestamp;
}
