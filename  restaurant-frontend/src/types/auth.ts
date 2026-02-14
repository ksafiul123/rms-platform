import { RoleName, UserInfo, RestaurantInfo } from "./user";

// Authentication Request Types
export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
  phoneNumber: string;
}

export interface RestaurantRegisterRequest {
  restaurantName: string;
  address: string;
  restaurantPhone: string;
  adminFullName: string;
  adminEmail: string;
  adminPassword: string;
  adminPhone: string;
  subscriptionPlanId?: number;
}

export interface TokenRefreshRequest {
  refreshToken: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

// Authentication Response Types
export interface AuthResponse {
  success: boolean;
  message: string;
  data: {
    accessToken: string;
    refreshToken: string;
    tokenType: string;
    expiresIn: number;
    user: UserInfo;
    restaurant?: RestaurantInfo;
  };
}

export interface TokenRefreshResponse {
  success: boolean;
  message: string;
  data: {
    accessToken: string;
    refreshToken: string;
    tokenType: string;
    expiresIn: number;
  };
}

// Auth State
export interface AuthState {
  isAuthenticated: boolean;
  user: UserInfo | null;
  restaurant: RestaurantInfo | null;
  accessToken: string | null;
  refreshToken: string | null;
  loading: boolean;
  error: string | null;
}

// Auth Context
export interface AuthContextType extends AuthState {
  login: (credentials: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => Promise<void>;
  refreshAccessToken: () => Promise<void>;
  clearError: () => void;
}