import { RoleName, PermissionName } from "@/types";

// Role Display Names
export const ROLE_DISPLAY_NAMES: Record<RoleName, string> = {
  [RoleName.ROLE_SUPER_ADMIN]: "Super Admin",
  [RoleName.ROLE_DEVELOPER]: "Developer",
  [RoleName.ROLE_SALESMAN]: "Salesman",
  [RoleName.ROLE_RESTAURANT_ADMIN]: "Restaurant Admin",
  [RoleName.ROLE_MANAGER]: "Manager",
  [RoleName.ROLE_CHEF]: "Chef",
  [RoleName.ROLE_DELIVERY_MAN]: "Delivery Man",
  [RoleName.ROLE_CUSTOMER]: "Customer",
};

// Role Levels (lower number = higher privilege)
export const ROLE_LEVELS: Record<RoleName, number> = {
  [RoleName.ROLE_SUPER_ADMIN]: 1,
  [RoleName.ROLE_DEVELOPER]: 2,
  [RoleName.ROLE_SALESMAN]: 3,
  [RoleName.ROLE_RESTAURANT_ADMIN]: 4,
  [RoleName.ROLE_MANAGER]: 5,
  [RoleName.ROLE_CHEF]: 6,
  [RoleName.ROLE_DELIVERY_MAN]: 7,
  [RoleName.ROLE_CUSTOMER]: 8,
};

// Permission Categories
export const PERMISSION_CATEGORIES = {
  MENU: "Menu Management",
  ORDER: "Order Management",
  INVENTORY: "Inventory Management",
  USER: "User Management",
  PERMISSION: "Permission Management",
  RESTAURANT: "Restaurant Management",
  FINANCIAL: "Financial Management",
  ANALYTICS: "Analytics",
  CUSTOMER: "Customer Management",
  TABLE: "Table Management",
} as const;

// Default Role Permissions
export const DEFAULT_ROLE_PERMISSIONS: Record<RoleName, PermissionName[]> = {
  [RoleName.ROLE_SUPER_ADMIN]: [
    // All permissions
    "menu:create", "menu:read", "menu:update", "menu:delete", "menu:approve", "menu:manage",
    "order:create", "order:read", "order:update", "order:cancel", "order:approve", "order:assign", "order:manage",
    "inventory:create", "inventory:read", "inventory:update", "inventory:delete", "inventory:manage",
    "user:create", "user:read", "user:update", "user:delete", "user:manage",
    "permission:view", "permission:override", "role:create", "role:assign", "role:manage",
    "restaurant:update", "restaurant:manage", "feature:toggle", "branch:create", "branch:manage",
    "payment:process", "payment:refund", "payment:view", "finance:report", "finance:manage",
    "analytics:view", "analytics:export",
    "customer:view", "customer:manage",
    "table:view", "table:manage",
  ],

  [RoleName.ROLE_RESTAURANT_ADMIN]: [
    "menu:create", "menu:read", "menu:update", "menu:delete", "menu:approve", "menu:manage",
    "order:create", "order:read", "order:update", "order:cancel", "order:approve", "order:assign", "order:manage",
    "inventory:create", "inventory:read", "inventory:update", "inventory:delete", "inventory:manage",
    "user:create", "user:read", "user:update", "user:delete", "user:manage",
    "role:assign",
    "restaurant:update", "restaurant:manage", "feature:toggle",
    "payment:process", "payment:view", "finance:report", "finance:manage",
    "analytics:view", "analytics:export",
    "customer:view", "customer:manage",
    "table:view", "table:manage",
  ],

  [RoleName.ROLE_MANAGER]: [
    "menu:read", "menu:update",
    "order:create", "order:read", "order:update", "order:assign", "order:manage",
    "inventory:read", "inventory:update",
    "user:read",
    "payment:process", "payment:view",
    "analytics:view",
    "customer:view",
    "table:view", "table:manage",
  ],

  [RoleName.ROLE_CHEF]: [
    "menu:read",
    "order:read", "order:update",
    "inventory:read",
  ],

  [RoleName.ROLE_DELIVERY_MAN]: [
    "order:read", "order:update",
  ],

  [RoleName.ROLE_CUSTOMER]: [
    "menu:read",
    "order:create", "order:read",
    "payment:view",
  ],

  [RoleName.ROLE_DEVELOPER]: [
    "analytics:view", "analytics:export",
  ],

  [RoleName.ROLE_SALESMAN]: [
    "restaurant:update",
    "user:create",
  ],
};