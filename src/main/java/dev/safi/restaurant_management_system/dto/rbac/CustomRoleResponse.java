package dev.safi.restaurant_management_system.dto.rbac;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Custom Role Response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Custom role details")
public class CustomRoleResponse {

    private Long id;
    private Long restaurantId;
    private String name;
    private String displayName;
    private String description;
    private String basedOnRoleName;
    private List<PermissionResponse> permissions;
    private Boolean isActive;
    private String createdByName;
    private LocalDateTime createdAt;
}
