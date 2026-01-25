package com.rms.controller;

//package com.rms.controller;

import com.rms.dto.auth.ApiResponse;
import com.rms.dto.rbac.*;
import dev.safi.restaurant_management_system.dto.rbac.*;
import com.rms.security.annotation.RequirePermission;
import com.rms.service.rbac.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Permission Controller
 * Manages permissions, roles, and access control
 */
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@Tag(name = "Permission Management", description = "RBAC and permission management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * Get all permissions grouped by category
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEVELOPER', 'RESTAURANT_ADMIN')")
    @Operation(
            summary = "Get all permissions",
            description = "Fetch all available permissions grouped by category"
    )
    public ResponseEntity<ApiResponse<List<PermissionCategoryResponse>>> getAllPermissions() {
        return ResponseEntity.ok(permissionService.getAllPermissionsGrouped());
    }

    /**
     * Get role with permissions
     */
    @GetMapping("/roles/{roleName}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEVELOPER', 'RESTAURANT_ADMIN')")
    @Operation(
            summary = "Get role details",
            description = "Fetch role with all assigned permissions"
    )
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleWithPermissions(
            @PathVariable String roleName) {
        return ResponseEntity.ok(permissionService.getRoleWithPermissions(roleName));
    }

    /**
     * Get role hierarchy
     */
    @GetMapping("/roles/hierarchy")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEVELOPER')")
    @Operation(
            summary = "Get role hierarchy",
            description = "Fetch complete role hierarchy with manageable roles"
    )
    public ResponseEntity<ApiResponse<List<RoleHierarchyResponse>>> getRoleHierarchy() {
        return ResponseEntity.ok(permissionService.getRoleHierarchy());
    }

    /**
     * Get user's effective permissions
     */
    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEVELOPER', 'RESTAURANT_ADMIN')")
    @Operation(
            summary = "Get user permissions",
            description = "Fetch all effective permissions for a user"
    )
    public ResponseEntity<ApiResponse<UserPermissionsResponse>> getUserPermissions(
            @PathVariable Long userId,
            @RequestParam(required = false) Long restaurantId) {
        return ResponseEntity.ok(permissionService.getUserPermissions(userId, restaurantId));
    }

    /**
     * Check if user has specific permission
     */
    @PostMapping("/check")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Check permission",
            description = "Check if user has a specific permission"
    )
    public ResponseEntity<ApiResponse<PermissionCheckResponse>> checkPermission(
            @Valid @RequestBody PermissionCheckRequest request) {

        boolean hasPermission = permissionService.hasPermission(
                request.getUserId(),
                request.getPermissionName(),
                request.getRestaurantId()
        );

        PermissionCheckResponse response = PermissionCheckResponse.builder()
                .hasPermission(hasPermission)
                .grantedThrough(hasPermission ? "role/override" : null)
                .details(hasPermission ? "Permission granted" : "Permission denied")
                .build();

        return ResponseEntity.ok(ApiResponse.success("Permission checked", response));
    }

    /**
     * Create custom role
     */
    @PostMapping("/custom-roles")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'RESTAURANT_ADMIN')")
    @Operation(
            summary = "Create custom role",
            description = "Create a restaurant-specific custom role"
    )
    public ResponseEntity<ApiResponse<CustomRoleResponse>> createCustomRole(
            @RequestParam Long restaurantId,
            @Valid @RequestBody CustomRoleRequest request) {
        return ResponseEntity.ok(
                permissionService.createCustomRole(restaurantId, request)
        );
    }

    /**
     * Get custom roles for restaurant
     */
    @GetMapping("/custom-roles")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'RESTAURANT_ADMIN', 'MANAGER')")
    @Operation(
            summary = "Get custom roles",
            description = "Fetch all custom roles for a restaurant"
    )
    public ResponseEntity<ApiResponse<List<CustomRoleResponse>>> getCustomRoles(
            @RequestParam Long restaurantId) {
        return ResponseEntity.ok(permissionService.getCustomRoles(restaurantId));
    }

    /**
     * Assign custom role to user
     */
    @PostMapping("/custom-roles/assign")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'RESTAURANT_ADMIN')")
    @RequirePermission(value = "user:manage", resource = "user_role")
    @Operation(
            summary = "Assign custom role",
            description = "Assign a custom role to a user"
    )
    public ResponseEntity<ApiResponse<Void>> assignCustomRole(
            @RequestParam Long userId,
            @RequestParam Long customRoleId,
            @RequestParam Long restaurantId) {
        return ResponseEntity.ok(
                permissionService.assignCustomRole(userId, customRoleId, restaurantId)
        );
    }

    /**
     * Grant permission override
     */
    @PostMapping("/overrides")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'RESTAURANT_ADMIN')")
    @RequirePermission(value = "permission:override", resource = "permission_override")
    @Operation(
            summary = "Grant permission override",
            description = "Grant or revoke permission override for a user"
    )
    public ResponseEntity<ApiResponse<PermissionOverrideResponse>> grantPermissionOverride(
            @Valid @RequestBody PermissionOverrideRequest request) {
        return ResponseEntity.ok(permissionService.grantPermissionOverride(request));
    }

    /**
     * Get audit logs
     */
    @GetMapping("/audit-logs")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEVELOPER', 'RESTAURANT_ADMIN')")
    @Operation(
            summary = "Get audit logs",
            description = "Fetch permission access audit logs"
    )
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long restaurantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(
                permissionService.getAuditLogs(userId, restaurantId, page, size)
        );
    }
}

