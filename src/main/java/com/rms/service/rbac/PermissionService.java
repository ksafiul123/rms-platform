package com.rms.service.rbac;

//package com.rms.service.rbac;

import com.rms.dto.auth.ApiResponse;
import com.rms.dto.rbac.*;
import com.rms.entity.*;
import com.rms.repository.*;
import dev.safi.restaurant_management_system.dto.rbac.*;
import dev.safi.restaurant_management_system.entity.*;
import com.rms.enums.AuditAction;
import com.rms.enums.OverrideType;
import com.rms.enums.RoleName;
import com.rms.exception.BadRequestException;
import com.rms.exception.ResourceNotFoundException;
import dev.safi.restaurant_management_system.repository.*;
import com.rms.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Permission Service - Manages permissions and access control
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final CustomRoleRepository customRoleRepository;
    private final UserCustomRoleRepository userCustomRoleRepository;
    private final PermissionOverrideRepository overrideRepository;
    private final PermissionAuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    /**
     * Get all permissions grouped by category
     */
    @Transactional(readOnly = true)
    public ApiResponse<List<PermissionCategoryResponse>> getAllPermissionsGrouped() {
        List<Permission> permissions = permissionRepository.findByIsActiveTrueOrderByCategoryAsc();

        Map<String, List<Permission>> grouped = permissions.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getCategory() != null ? p.getCategory() : "OTHER"
                ));

        List<PermissionCategoryResponse> response = grouped.entrySet().stream()
                .map(entry -> PermissionCategoryResponse.builder()
                        .category(entry.getKey())
                        .displayName(formatCategoryName(entry.getKey()))
                        .permissions(entry.getValue().stream()
                                .map(this::mapToPermissionResponse)
                                .collect(Collectors.toList()))
                        .build())
                .sorted(Comparator.comparing(PermissionCategoryResponse::getCategory))
                .collect(Collectors.toList());

        return ApiResponse.success("Permissions fetched successfully", response);
    }

    /**
     * Get role with permissions
     */
    @Transactional(readOnly = true)
    public ApiResponse<RoleResponse> getRoleWithPermissions(String roleName) {
        Role role = roleRepository.findByNameWithPermissions(RoleName.valueOf(roleName))
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        return ApiResponse.success("Role fetched successfully", mapToRoleResponse(role));
    }

    /**
     * Get all system roles with hierarchy
     */
    @Transactional(readOnly = true)
    public ApiResponse<List<RoleHierarchyResponse>> getRoleHierarchy() {
        List<Role> roles = roleRepository.findByIsActiveTrueOrderByRoleLevelAsc();

        List<RoleHierarchyResponse> hierarchy = roles.stream()
                .map(role -> {
                    List<String> managableRoles = roleRepository.findManageableRoles(role.getRoleLevel())
                            .stream()
                            .map(r -> r.getName().name())
                            .collect(Collectors.toList());

                    return RoleHierarchyResponse.builder()
                            .roleName(role.getName().name())
                            .roleLevel(role.getRoleLevel())
                            .managableRoles(managableRoles)
                            .permissionCount(role.getPermissions().size())
                            .build();
                })
                .collect(Collectors.toList());

        return ApiResponse.success("Role hierarchy fetched successfully", hierarchy);
    }

    /**
     * Check if user has specific permission
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "userPermissions", key = "#userId + '_' + #permissionName + '_' + #restaurantId")
    public boolean hasPermission(Long userId, String permissionName, Long restaurantId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Super Admin has all permissions
        if (hasSystemRole(user, RoleName.ROLE_SUPER_ADMIN)) {
            return true;
        }

        // Check system roles
        for (Role role : user.getRoles()) {
            if (role.getPermissions().stream()
                    .anyMatch(p -> p.getName().equals(permissionName) && p.getIsActive())) {
                return true;
            }
        }

        // Check custom roles (restaurant-specific)
        if (restaurantId != null) {
            List<UserCustomRole> customRoles = userCustomRoleRepository
                    .findByUserIdAndRestaurantIdWithPermissions(userId, restaurantId);

            for (UserCustomRole ucr : customRoles) {
                if (ucr.getCustomRole().getPermissions().stream()
                        .anyMatch(p -> p.getName().equals(permissionName) && p.getIsActive())) {
                    return true;
                }
            }
        }

        // Check permission overrides
        List<PermissionOverride> overrides = overrideRepository
                .findActiveByUserId(userId, LocalDateTime.now());

        for (PermissionOverride override : overrides) {
            if (override.getPermission().getName().equals(permissionName)) {
                return override.getOverrideType() == OverrideType.GRANT;
            }
        }

        return false;
    }

    /**
     * Get all effective permissions for user
     */
    @Transactional(readOnly = true)
    public ApiResponse<UserPermissionsResponse> getUserPermissions(Long userId, Long restaurantId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Set<Permission> allPermissions = new HashSet<>();

        // System role permissions
        user.getRoles().forEach(role -> {
            Role fullRole = roleRepository.findByIdWithPermissions(role.getId()).orElse(role);
            allPermissions.addAll(fullRole.getPermissions());
        });

        // Custom role permissions
        List<UserCustomRole> customRoles = new ArrayList<>();
        if (restaurantId != null) {
            customRoles = userCustomRoleRepository
                    .findByUserIdAndRestaurantIdWithPermissions(userId, restaurantId);

            customRoles.forEach(ucr ->
                    allPermissions.addAll(ucr.getCustomRole().getPermissions())
            );
        }

        // Apply overrides
        List<PermissionOverride> overrides = overrideRepository
                .findActiveByUserId(userId, LocalDateTime.now());

        for (PermissionOverride override : overrides) {
            if (override.getOverrideType() == OverrideType.GRANT) {
                allPermissions.add(override.getPermission());
            } else {
                allPermissions.remove(override.getPermission());
            }
        }

        UserPermissionsResponse response = UserPermissionsResponse.builder()
                .userId(userId)
                .userName(user.getFullName())
                .restaurantId(restaurantId)
                .systemRoles(user.getRoles().stream()
                        .map(r -> r.getName().name())
                        .collect(Collectors.toList()))
                .customRoles(customRoles.stream()
                        .map(this::mapToCustomRoleResponse)
                        .collect(Collectors.toList()))
                .permissions(allPermissions.stream()
                        .filter(Permission::getIsActive)
                        .map(this::mapToPermissionResponse)
                        .collect(Collectors.toList()))
                .overrides(overrides.stream()
                        .map(this::mapToOverrideResponse)
                        .collect(Collectors.toList()))
                .build();

        return ApiResponse.success("User permissions fetched successfully", response);
    }

    /**
     * Create custom role
     */
    @Transactional
    @CacheEvict(value = "userPermissions", allEntries = true)
    public ApiResponse<CustomRoleResponse> createCustomRole(
            Long restaurantId,
            CustomRoleRequest request) {

        if (customRoleRepository.existsByRestaurantIdAndName(restaurantId, request.getName())) {
            throw new BadRequestException("Custom role with this name already exists");
        }

        Role basedOnRole = null;
        if (request.getBasedOnRoleId() != null) {
            basedOnRole = roleRepository.findById(request.getBasedOnRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Base role not found"));
        }

        Set<Permission> permissions = new HashSet<>(
                permissionRepository.findAllById(request.getPermissionIds())
        );

        CustomRole customRole = CustomRole.builder()
                .restaurantId(restaurantId)
                .name(request.getName())
                .displayName(request.getDisplayName())
                .description(request.getDescription())
                .basedOnRole(basedOnRole)
                .permissions(permissions)
                .isActive(true)
                .createdBy(getCurrentUserId())
                .build();

        customRole = customRoleRepository.save(customRole);

        log.info("Custom role created: {} for restaurant ID: {}", request.getName(), restaurantId);

        logAudit(AuditAction.CUSTOM_ROLE_CREATED, "custom_role",
                "Created custom role: " + request.getName());

        return ApiResponse.success("Custom role created successfully",
                mapToCustomRoleResponse(customRole));
    }

    /**
     * Assign custom role to user
     */
    @Transactional
    @CacheEvict(value = "userPermissions", key = "#userId + '_*'")
    public ApiResponse<Void> assignCustomRole(Long userId, Long customRoleId, Long restaurantId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        CustomRole customRole = customRoleRepository.findByIdAndRestaurantId(customRoleId, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Custom role not found"));

        if (userCustomRoleRepository.existsByUserIdAndCustomRoleIdAndIsActiveTrue(userId, customRoleId)) {
            throw new BadRequestException("User already has this custom role");
        }

        UserCustomRole assignment = UserCustomRole.builder()
                .userId(userId)
                .restaurantId(restaurantId)
                .customRole(customRole)
                .assignedBy(getCurrentUserId())
                .assignedAt(LocalDateTime.now())
                .isActive(true)
                .build();

        userCustomRoleRepository.save(assignment);

        log.info("Custom role {} assigned to user {} for restaurant {}",
                customRole.getName(), userId, restaurantId);

        logAudit(AuditAction.ROLE_ASSIGNED, "user",
                "Assigned custom role: " + customRole.getName() + " to user: " + user.getFullName());

        return ApiResponse.success("Custom role assigned successfully", null);
    }

    /**
     * Grant permission override
     */
    @Transactional
    @CacheEvict(value = "userPermissions", key = "#request.userId + '_*'")
    public ApiResponse<PermissionOverrideResponse> grantPermissionOverride(
            PermissionOverrideRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Permission permission = permissionRepository.findById(request.getPermissionId())
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found"));

        UserPrincipal currentUser = getCurrentUser();

        PermissionOverride override = PermissionOverride.builder()
                .userId(request.getUserId())
                .restaurantId(currentUser.getRestaurantId())
                .permission(permission)
                .overrideType(OverrideType.valueOf(request.getOverrideType()))
                .grantedBy(getCurrentUserId())
                .expiresAt(request.getExpiresAt())
                .reason(request.getReason())
                .isActive(true)
                .build();

        override = overrideRepository.save(override);

        log.info("Permission override {} for permission {} to user {}",
                request.getOverrideType(), permission.getName(), user.getFullName());

        logAudit(AuditAction.PERMISSION_OVERRIDE_GRANTED, "permission_override",
                String.format("%s permission: %s to user: %s",
                        request.getOverrideType(), permission.getName(), user.getFullName()));

        return ApiResponse.success("Permission override granted successfully",
                mapToOverrideResponse(override));
    }

    /**
     * Get custom roles for restaurant
     */
    @Transactional(readOnly = true)
    public ApiResponse<List<CustomRoleResponse>> getCustomRoles(Long restaurantId) {
        List<CustomRole> customRoles = customRoleRepository
                .findByRestaurantIdWithPermissions(restaurantId);

        List<CustomRoleResponse> responses = customRoles.stream()
                .map(this::mapToCustomRoleResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("Custom roles fetched successfully", responses);
    }

    /**
     * Get audit logs
     */
    @Transactional(readOnly = true)
    public ApiResponse<List<AuditLogResponse>> getAuditLogs(
            Long userId,
            Long restaurantId,
            int page,
            int size) {

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(page, size);

        org.springframework.data.domain.Page<PermissionAuditLog> logs;

        if (userId != null && restaurantId != null) {
            logs = auditLogRepository.findByUserIdAndRestaurantIdOrderByTimestampDesc(
                    userId, restaurantId, pageable);
        } else if (userId != null) {
            logs = auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
        } else if (restaurantId != null) {
            logs = auditLogRepository.findByRestaurantIdOrderByTimestampDesc(restaurantId, pageable);
        } else {
            logs = auditLogRepository.findAll(pageable);
        }

        List<AuditLogResponse> responses = logs.stream()
                .map(this::mapToAuditLogResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("Audit logs fetched successfully", responses);
    }

    // ==================== Helper Methods ====================

    private boolean hasSystemRole(User user, RoleName roleName) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName() == roleName);
    }

    private void logAudit(AuditAction action, String resource, String details) {
        UserPrincipal principal = getCurrentUser();

        PermissionAuditLog log = PermissionAuditLog.builder()
                .userId(principal.getId())
                .restaurantId(principal.getRestaurantId())
                .action(action)
                .resource(resource)
                .accessGranted(true)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();

        auditLogRepository.save(log);
    }

    private String formatCategoryName(String category) {
        return Arrays.stream(category.split("_"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    private PermissionResponse mapToPermissionResponse(Permission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .displayName(permission.getDisplayName())
                .description(permission.getDescription())
                .resource(permission.getResource())
                .action(permission.getAction().name())
                .category(permission.getCategory())
                .isSystem(permission.getIsSystem())
                .isActive(permission.getIsActive())
                .build();
    }

    private RoleResponse mapToRoleResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName().name())
                .displayName(role.getDisplayName())
                .description(role.getDescription())
                .roleLevel(role.getRoleLevel())
                .permissions(role.getPermissions().stream()
                        .map(this::mapToPermissionResponse)
                        .collect(Collectors.toList()))
                .isSystem(role.getIsSystem())
                .isActive(role.getIsActive())
                .createdAt(role.getCreatedAt())
                .build();
    }

    private CustomRoleResponse mapToCustomRoleResponse(CustomRole customRole) {
        String createdByName = null;
        if (customRole.getCreatedBy() != null) {
            createdByName = userRepository.findById(customRole.getCreatedBy())
                    .map(User::getFullName)
                    .orElse(null);
        }

        return CustomRoleResponse.builder()
                .id(customRole.getId())
                .restaurantId(customRole.getRestaurantId())
                .name(customRole.getName())
                .displayName(customRole.getDisplayName())
                .description(customRole.getDescription())
                .basedOnRoleName(customRole.getBasedOnRole() != null ?
                        customRole.getBasedOnRole().getName().name() : null)
                .permissions(customRole.getPermissions().stream()
                        .map(this::mapToPermissionResponse)
                        .collect(Collectors.toList()))
                .isActive(customRole.getIsActive())
                .createdByName(createdByName)
                .createdAt(customRole.getCreatedAt())
                .build();
    }

    private CustomRoleResponse mapToCustomRoleResponse(UserCustomRole ucr) {
        return mapToCustomRoleResponse(ucr.getCustomRole());
    }

    private PermissionOverrideResponse mapToOverrideResponse(PermissionOverride override) {
        String grantedByName = null;
        if (override.getGrantedBy() != null) {
            grantedByName = userRepository.findById(override.getGrantedBy())
                    .map(User::getFullName)
                    .orElse(null);
        }

        return PermissionOverrideResponse.builder()
                .id(override.getId())
                .userId(override.getUserId())
                .permission(mapToPermissionResponse(override.getPermission()))
                .overrideType(override.getOverrideType().name())
                .expiresAt(override.getExpiresAt())
                .reason(override.getReason())
                .grantedByName(grantedByName)
                .createdAt(override.getCreatedAt())
                .build();
    }

    private AuditLogResponse mapToAuditLogResponse(PermissionAuditLog log) {
        String userName = userRepository.findById(log.getUserId())
                .map(User::getFullName)
                .orElse("Unknown");

        return AuditLogResponse.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .userName(userName)
                .restaurantId(log.getRestaurantId())
                .action(log.getAction().name())
                .resource(log.getResource())
                .endpoint(log.getEndpoint())
                .permissionName(log.getPermissionName())
                .accessGranted(log.getAccessGranted())
                .ipAddress(log.getIpAddress())
                .details(log.getDetails())
                .timestamp(log.getTimestamp())
                .build();
    }

    private UserPrincipal getCurrentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    private Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
