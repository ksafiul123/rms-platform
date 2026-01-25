package com.rms.security;

public class SecurityUtil {

    public static UserPrincipal getCurrentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public static Long getCurrentRestaurantId() {
        return getCurrentUser().getRestaurantId();
    }

    public static boolean hasRole(String roleName) {
        return getCurrentUser().getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(roleName));
    }

    public static boolean isSuperAdmin() {
        return hasRole("ROLE_SUPER_ADMIN");
    }
}
