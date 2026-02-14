// API Configuration
export const API_CONFIG = {
  BASE_URL: process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api/v1",
  TIMEOUT: parseInt(process.env.NEXT_PUBLIC_API_TIMEOUT || "30000"),

  // API Endpoints
  ENDPOINTS: {
    // Auth
    AUTH: {
      LOGIN: "/auth/login",
      REGISTER: "/auth/register",
      REGISTER_RESTAURANT: "/auth/register-restaurant",
      REFRESH_TOKEN: "/auth/refresh-token",
      LOGOUT: "/auth/logout",
      ME: "/auth/me",
      FORGOT_PASSWORD: "/auth/forgot-password",
      RESET_PASSWORD: "/auth/reset-password",
    },

    // Restaurant
    RESTAURANT: {
      BASE: "/restaurants",
      SETTINGS: "/restaurants/settings",
      FEATURES: "/restaurants/features",
      BRANCHES: "/restaurants/branches",
    },

    // Users
    USERS: {
      BASE: "/users",
      BY_ID: (id: number) => `/users/${id}`,
      BY_RESTAURANT: "/users/restaurant",
    },

    // Permissions
    PERMISSIONS: {
      BASE: "/permissions",
      USER: (userId: number) => `/permissions/users/${userId}`,
      CUSTOM_ROLES: "/permissions/custom-roles",
      OVERRIDES: "/permissions/overrides",
      AUDIT_LOGS: "/permissions/audit-logs",
    },

    // Menu
    MENU: {
      CATEGORIES: "/menu/categories",
      CATEGORY_BY_ID: (id: number) => `/menu/categories/${id}`,
      ITEMS: "/menu/items",
      ITEM_BY_ID: (id: number) => `/menu/items/${id}`,
      MODIFIERS: "/menu/modifiers",
      MODIFIER_BY_ID: (id: number) => `/menu/modifiers/${id}`,
      PUBLIC: "/public/menu",
    },

    // Orders
    ORDERS: {
      BASE: "/orders",
      BY_ID: (id: number) => `/orders/${id}`,
      STATUS: (id: number) => `/orders/${id}/status`,
      ASSIGN: (id: number) => `/orders/${id}/assign`,
    },

    // Inventory
    INVENTORY: {
      BASE: "/inventory",
      BY_ID: (id: number) => `/inventory/${id}`,
      ALERTS: "/inventory/alerts",
    },

    // Analytics
    ANALYTICS: {
      DASHBOARD: "/analytics/dashboard",
      SALES: "/analytics/sales",
      ORDERS: "/analytics/orders",
      EXPORT: "/analytics/export",
    },
  },
} as const;

// Token Configuration
export const TOKEN_CONFIG = {
  ACCESS_TOKEN_KEY: "access_token",
  REFRESH_TOKEN_KEY: "refresh_token",
  TOKEN_TYPE: "Bearer",
  REFRESH_INTERVAL: parseInt(
    process.env.NEXT_PUBLIC_TOKEN_REFRESH_INTERVAL || "840000"
  ), // 14 minutes
} as const;

// App Configuration
export const APP_CONFIG = {
  NAME: process.env.NEXT_PUBLIC_APP_NAME || "Restaurant Management System",
  URL: process.env.NEXT_PUBLIC_APP_URL || "http://localhost:3000",
  SESSION_TIMEOUT: parseInt(
    process.env.NEXT_PUBLIC_SESSION_TIMEOUT || "3600000"
  ), // 1 hour
} as const;