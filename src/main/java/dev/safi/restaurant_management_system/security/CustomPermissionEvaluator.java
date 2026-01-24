package dev.safi.restaurant_management_system.security;

@Component("permissionEvaluator")
@RequiredArgsConstructor
public class CustomPermissionEvaluator {

    private final PermissionService permissionService;

    /**
     * Check if current user has permission
     * Usage: @PreAuthorize("@permissionEvaluator.hasPermission('menu:create')")
     */
    public boolean hasPermission(String permission) {
        UserPrincipal principal = SecurityUtil.getCurrentUser();
        return permissionService.hasPermission(
                principal.getId(),
                permission,
                principal.getRestaurantId()
        );
    }

    /**
     * Check if user has any of the permissions
     */
    public boolean hasAnyPermission(String... permissions) {
        UserPrincipal principal = SecurityUtil.getCurrentUser();
        for (String permission : permissions) {
            if (permissionService.hasPermission(
                    principal.getId(),
                    permission,
                    principal.getRestaurantId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if user has all permissions
     */
    public boolean hasAllPermissions(String... permissions) {
        UserPrincipal principal = SecurityUtil.getCurrentUser();
        for (String permission : permissions) {
            if (!permissionService.hasPermission(
                    principal.getId(),
                    permission,
                    principal.getRestaurantId())) {
                return false;
            }
        }
        return true;
    }
}
