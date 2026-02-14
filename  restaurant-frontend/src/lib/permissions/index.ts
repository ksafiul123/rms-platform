import { RoleName, PermissionName, UserInfo } from "@/types";
import { DEFAULT_ROLE_PERMISSIONS, ROLE_LEVELS } from "@/constants/permissions";

/**
 * Check if user has a specific permission
 */
export const hasPermission = (
  user: UserInfo | null,
  permission: PermissionName
): boolean => {
  if (!user) return false;

  // Super Admin has all permissions
  if (user.role === RoleName.ROLE_SUPER_ADMIN) {
    return true;
  }

  // Check if user's role has the permission
  const rolePermissions = DEFAULT_ROLE_PERMISSIONS[user.role] || [];
  return rolePermissions.includes(permission);
};

/**
 * Check if user has any of the specified permissions
 */
export const hasAnyPermission = (
  user: UserInfo | null,
  permissions: PermissionName[]
): boolean => {
  if (!user) return false;
  return permissions.some((permission) => hasPermission(user, permission));
};

/**
 * Check if user has all of the specified permissions
 */
export const hasAllPermissions = (
  user: UserInfo | null,
  permissions: PermissionName[]
): boolean => {
  if (!user) return false;
  return permissions.every((permission) => hasPermission(user, permission));
};

/**
 * Check if user's role level is sufficient
 * Lower number = higher privilege
 */
export const hasRoleLevel = (
  user: UserInfo | null,
  requiredLevel: number
): boolean => {
  if (!user) return false;

  const userLevel = ROLE_LEVELS[user.role];
  return userLevel <= requiredLevel;
};

/**
 * Check if user has a specific role
 */
export const hasRole = (user: UserInfo | null, role: RoleName): boolean => {
  if (!user) return false;
  return user.role === role;
};

/**
 * Check if user has any of the specified roles
 */
export const hasAnyRole = (
  user: UserInfo | null,
  roles: RoleName[]
): boolean => {
  if (!user) return false;
  return roles.includes(user.role);
};

/**
 * Check if user is admin (Restaurant Admin or Super Admin)
 */
export const isAdmin = (user: UserInfo | null): boolean => {
  if (!user) return false;
  return hasAnyRole(user, [
    RoleName.ROLE_SUPER_ADMIN,
    RoleName.ROLE_RESTAURANT_ADMIN,
  ]);
};

/**
 * Check if user is staff (non-customer role)
 */
export const isStaff = (user: UserInfo | null): boolean => {
  if (!user) return false;
  return user.role !== RoleName.ROLE_CUSTOMER;
};

/**
 * Get user's permissions
 */
export const getUserPermissions = (user: UserInfo | null): PermissionName[] => {
  if (!user) return [];

  // Super Admin has all permissions
  if (user.role === RoleName.ROLE_SUPER_ADMIN) {
    return DEFAULT_ROLE_PERMISSIONS[RoleName.ROLE_SUPER_ADMIN];
  }

  return DEFAULT_ROLE_PERMISSIONS[user.role] || [];
};

/**
 * Check if user can access a route
 */
export const canAccessRoute = (
  user: UserInfo | null,
  routePath: string,
  allowedRoles?: RoleName[]
): boolean => {
  if (!user) return false;

  // If no specific roles required, any authenticated user can access
  if (!allowedRoles || allowedRoles.length === 0) {
    return true;
  }

  return hasAnyRole(user, allowedRoles);
};