package dev.safi.restaurant_management_system.security.aspect;

import dev.safi.restaurant_management_system.entity.PermissionAuditLog;
import dev.safi.restaurant_management_system.enums.AuditAction;
import dev.safi.restaurant_management_system.repository.PermissionAuditLogRepository;
import dev.safi.restaurant_management_system.security.UserPrincipal;
import dev.safi.restaurant_management_system.security.annotation.RequireAllPermissions;
import dev.safi.restaurant_management_system.security.annotation.RequireAnyPermission;
import dev.safi.restaurant_management_system.security.annotation.RequirePermission;
import dev.safi.restaurant_management_system.security.annotation.RequireRoleLevel;
import dev.safi.restaurant_management_system.service.rbac.PermissionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.classfile.MethodSignature;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionCheckAspect {

    private final PermissionService permissionService;
    private final PermissionAuditLogRepository auditLogRepository;

    /**
     * Check single permission
     */
    @Around("@annotation(com.rms.security.annotation.RequirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequirePermission annotation = method.getAnnotation(RequirePermission.class);

        UserPrincipal principal = getCurrentUser();
        String permissionName = annotation.value();

        boolean hasPermission = permissionService.hasPermission(
                principal.getId(),
                permissionName,
                principal.getRestaurantId()
        );

        if (annotation.audit()) {
            logAccess(principal, permissionName, annotation.resource(), hasPermission);
        }

        if (!hasPermission) {
            log.warn("Access denied: User {} does not have permission: {}",
                    principal.getEmail(), permissionName);
            throw new InsufficientPermissionException(
                    "You do not have permission to perform this action: " + permissionName);
        }

        log.debug("Permission check passed: {} for user {}", permissionName, principal.getEmail());
        return joinPoint.proceed();
    }

    /**
     * Check any of multiple permissions
     */
    @Around("@annotation(com.rms.security.annotation.RequireAnyPermission)")
    public Object checkAnyPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireAnyPermission annotation = method.getAnnotation(RequireAnyPermission.class);

        UserPrincipal principal = getCurrentUser();
        String[] permissions = annotation.value();

        boolean hasAnyPermission = false;
        for (String permission : permissions) {
            if (permissionService.hasPermission(
                    principal.getId(),
                    permission,
                    principal.getRestaurantId())) {
                hasAnyPermission = true;
                break;
            }
        }

        if (annotation.audit()) {
            logAccess(principal, String.join(" OR ", permissions),
                    annotation.resource(), hasAnyPermission);
        }

        if (!hasAnyPermission) {
            log.warn("Access denied: User {} does not have any of permissions: {}",
                    principal.getEmail(), String.join(", ", permissions));
            throw new InsufficientPermissionException(
                    "You do not have required permissions");
        }

        return joinPoint.proceed();
    }

    /**
     * Check all of multiple permissions
     */
    @Around("@annotation(com.rms.security.annotation.RequireAllPermissions)")
    public Object checkAllPermissions(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireAllPermissions annotation = method.getAnnotation(RequireAllPermissions.class);

        UserPrincipal principal = getCurrentUser();
        String[] permissions = annotation.value();

        for (String permission : permissions) {
            if (!permissionService.hasPermission(
                    principal.getId(),
                    permission,
                    principal.getRestaurantId())) {

                if (annotation.audit()) {
                    logAccess(principal, String.join(" AND ", permissions),
                            annotation.resource(), false);
                }

                log.warn("Access denied: User {} missing permission: {}",
                        principal.getEmail(), permission);
                throw new InsufficientPermissionException(
                        "You do not have all required permissions");
            }
        }

        if (annotation.audit()) {
            logAccess(principal, String.join(" AND ", permissions),
                    annotation.resource(), true);
        }

        return joinPoint.proceed();
    }

    /**
     * Check role level
     */
    @Around("@annotation(com.rms.security.annotation.RequireRoleLevel)")
    public Object checkRoleLevel(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireRoleLevel annotation = method.getAnnotation(RequireRoleLevel.class);

        UserPrincipal principal = getCurrentUser();
        int requiredLevel = annotation.value();

        // Get user's highest role level
        // This would need to be implemented based on your role hierarchy
        // For now, we'll allow Super Admin (assumed level 1) to bypass

        boolean hasAccess = principal.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_SUPER_ADMIN"));

        if (annotation.audit()) {
            logAccess(principal, "ROLE_LEVEL_" + requiredLevel, "role_level", hasAccess);
        }

        if (!hasAccess) {
            throw new InsufficientPermissionException(
                    "Insufficient role level for this operation");
        }

        return joinPoint.proceed();
    }

    /**
     * Log access attempt
     */
    private void logAccess(UserPrincipal principal, String permissionName,
                           String resource, boolean granted) {
        try {
            HttpServletRequest request = getCurrentRequest();

            PermissionAuditLog log = PermissionAuditLog.builder()
                    .userId(principal.getId())
                    .restaurantId(principal.getRestaurantId())
                    .action(granted ? AuditAction.ACCESS_GRANTED : AuditAction.ACCESS_DENIED)
                    .resource(resource)
                    .endpoint(request != null ? request.getRequestURI() : null)
                    .permissionName(permissionName)
                    .accessGranted(granted)
                    .ipAddress(request != null ? getClientIp(request) : null)
                    .userAgent(request != null ? request.getHeader("User-Agent") : null)
                    .timestamp(LocalDateTime.now())
                    .build();

            auditLogRepository.save(log);
        } catch (Exception e) {
            this.log.error("Failed to log access attempt", e);
        }
    }

    private UserPrincipal getCurrentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
