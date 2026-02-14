// Permission Types
export type PermissionName =
  // Menu Management
  | "menu:create"
  | "menu:read"
  | "menu:update"
  | "menu:delete"
  | "menu:approve"
  | "menu:manage"
  // Order Management
  | "order:create"
  | "order:read"
  | "order:update"
  | "order:cancel"
  | "order:approve"
  | "order:assign"
  | "order:manage"
  // Inventory Management
  | "inventory:create"
  | "inventory:read"
  | "inventory:update"
  | "inventory:delete"
  | "inventory:manage"
  // User Management
  | "user:create"
  | "user:read"
  | "user:update"
  | "user:delete"
  | "user:manage"
  // Permission Management
  | "permission:view"
  | "permission:override"
  | "role:create"
  | "role:assign"
  | "role:manage"
  // Restaurant Management
  | "restaurant:update"
  | "restaurant:manage"
  | "feature:toggle"
  | "branch:create"
  | "branch:manage"
  // Financial Management
  | "payment:process"
  | "payment:refund"
  | "payment:view"
  | "finance:report"
  | "finance:manage"
  // Analytics
  | "analytics:view"
  | "analytics:export"
  // Customer Management
  | "customer:view"
  | "customer:manage"
  // Table Management
  | "table:view"
  | "table:manage";

export interface Permission {
  id: number;
  name: PermissionName;
  displayName: string;
  description: string;
  category: string;
  isSystemPermission: boolean;
  isActive: boolean;
}

export interface RolePermissions {
  roleId: number;
  roleName: string;
  permissions: Permission[];
}

export interface UserPermissions {
  userId: number;
  userName: string;
  restaurantId: number;
  systemRoles: string[];
  customRoles: CustomRole[];
  permissions: Permission[];
  overrides: PermissionOverride[];
}

export interface CustomRole {
  id: number;
  name: string;
  displayName: string;
  description: string;
  restaurantId: number;
  basedOnRoleId?: number;
  permissions: Permission[];
  isActive: boolean;
}

export interface PermissionOverride {
  id: number;
  userId: number;
  permissionId: number;
  overrideType: "GRANT" | "REVOKE";
  reason: string;
  expiresAt?: string;
  createdAt: string;
}