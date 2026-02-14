import { RoleName } from "@/types";

// Route Configuration
export const ROUTES = {
  // Public Routes
  HOME: "/",
  LOGIN: "/login",
  REGISTER: "/register",
  FORGOT_PASSWORD: "/forgot-password",
  RESET_PASSWORD: "/reset-password",

  // Protected Routes
  DASHBOARD: "/dashboard",

  // Menu Routes
  MENU: "/menu",
  MENU_CATEGORIES: "/menu/categories",
  MENU_ITEMS: "/menu/items",
  MENU_MODIFIERS: "/menu/modifiers",

  // Order Routes
  ORDERS: "/orders",
  ORDER_DETAILS: (id: number) => `/orders/${id}`,

  // Inventory Routes
  INVENTORY: "/inventory",

  // User Routes
  USERS: "/users",
  USER_DETAILS: (id: number) => `/users/${id}`,

  // Settings Routes
  SETTINGS: "/settings",
  RESTAURANT_SETTINGS: "/restaurant",

  // Analytics Routes
  ANALYTICS: "/analytics",

  // Payment Routes
  PAYMENTS: "/payments",
} as const;

// Public Routes (no authentication required)
export const PUBLIC_ROUTES = [
  ROUTES.HOME,
  ROUTES.LOGIN,
  ROUTES.REGISTER,
  ROUTES.FORGOT_PASSWORD,
  ROUTES.RESET_PASSWORD,
];

// Protected Routes (authentication required)
export const PROTECTED_ROUTES = [
  ROUTES.DASHBOARD,
  ROUTES.MENU,
  ROUTES.ORDERS,
  ROUTES.INVENTORY,
  ROUTES.USERS,
  ROUTES.SETTINGS,
  ROUTES.ANALYTICS,
  ROUTES.PAYMENTS,
];

// Role-based Route Access
export const ROLE_ROUTES: Record<RoleName, string[]> = {
  [RoleName.ROLE_SUPER_ADMIN]: [
    ROUTES.DASHBOARD,
    ROUTES.MENU,
    ROUTES.ORDERS,
    ROUTES.INVENTORY,
    ROUTES.USERS,
    ROUTES.SETTINGS,
    ROUTES.ANALYTICS,
    ROUTES.PAYMENTS,
    ROUTES.RESTAURANT_SETTINGS,
  ],

  [RoleName.ROLE_DEVELOPER]: [
    ROUTES.DASHBOARD,
    ROUTES.ANALYTICS,
    ROUTES.SETTINGS,
  ],

  [RoleName.ROLE_SALESMAN]: [
    ROUTES.DASHBOARD,
    ROUTES.RESTAURANT_SETTINGS,
  ],

  [RoleName.ROLE_RESTAURANT_ADMIN]: [
    ROUTES.DASHBOARD,
    ROUTES.MENU,
    ROUTES.ORDERS,
    ROUTES.INVENTORY,
    ROUTES.USERS,
    ROUTES.SETTINGS,
    ROUTES.ANALYTICS,
    ROUTES.PAYMENTS,
  ],

  [RoleName.ROLE_MANAGER]: [
    ROUTES.DASHBOARD,
    ROUTES.MENU,
    ROUTES.ORDERS,
    ROUTES.INVENTORY,
    ROUTES.ANALYTICS,
  ],

  [RoleName.ROLE_CHEF]: [
    ROUTES.DASHBOARD,
    ROUTES.ORDERS,
  ],

  [RoleName.ROLE_DELIVERY_MAN]: [
    ROUTES.DASHBOARD,
    ROUTES.ORDERS,
  ],

  [RoleName.ROLE_CUSTOMER]: [
    ROUTES.DASHBOARD,
    ROUTES.ORDERS,
  ],
};

// Dashboard Routes by Role
export const ROLE_DASHBOARD_ROUTES: Record<RoleName, string> = {
  [RoleName.ROLE_SUPER_ADMIN]: "/dashboard/super-admin",
  [RoleName.ROLE_DEVELOPER]: "/dashboard",
  [RoleName.ROLE_SALESMAN]: "/dashboard",
  [RoleName.ROLE_RESTAURANT_ADMIN]: "/dashboard/restaurant-admin",
  [RoleName.ROLE_MANAGER]: "/dashboard/manager",
  [RoleName.ROLE_CHEF]: "/dashboard/chef",
  [RoleName.ROLE_DELIVERY_MAN]: "/dashboard/delivery",
  [RoleName.ROLE_CUSTOMER]: "/dashboard/customer",
};