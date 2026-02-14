// User Types
export enum RoleName {
  ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN",
  ROLE_DEVELOPER = "ROLE_DEVELOPER",
  ROLE_SALESMAN = "ROLE_SALESMAN",
  ROLE_RESTAURANT_ADMIN = "ROLE_RESTAURANT_ADMIN",
  ROLE_MANAGER = "ROLE_MANAGER",
  ROLE_CHEF = "ROLE_CHEF",
  ROLE_DELIVERY_MAN = "ROLE_DELIVERY_MAN",
  ROLE_CUSTOMER = "ROLE_CUSTOMER",
}

export interface User {
  id: number;
  email: string;
  fullName: string;
  phoneNumber: string;
  role: RoleName;
  restaurantId?: number;
  isActive: boolean;
  isEmailVerified: boolean;
  isPhoneVerified: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface UserInfo {
  id: number;
  email: string;
  fullName: string;
  phoneNumber: string;
  role: RoleName;
  restaurantId?: number;
  isActive: boolean;
}

export interface RestaurantInfo {
  id: number;
  name: string;
  restaurantCode: string;
  address: string;
  phoneNumber: string;
  subscriptionStatus: string;
  subscriptionPlan?: string;
  expiresAt?: string;
}