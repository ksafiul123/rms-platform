import { api } from "./client";
import { API_CONFIG } from "@/config/api.config";
import {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  RestaurantRegisterRequest,
  TokenRefreshRequest,
  TokenRefreshResponse,
  ForgotPasswordRequest,
  ResetPasswordRequest,
  ApiResponse,
  UserInfo,
} from "@/types";

export const authService = {
  /**
   * Login user
   */
  login: async (credentials: LoginRequest): Promise<AuthResponse> => {
    const response = await api.post<AuthResponse>(
      API_CONFIG.ENDPOINTS.AUTH.LOGIN,
      credentials
    );
    return response.data;
  },

  /**
   * Register new customer
   */
  register: async (data: RegisterRequest): Promise<AuthResponse> => {
    const response = await api.post<AuthResponse>(
      API_CONFIG.ENDPOINTS.AUTH.REGISTER,
      data
    );
    return response.data;
  },

  /**
   * Register new restaurant (Admin/Salesman only)
   */
  registerRestaurant: async (
    data: RestaurantRegisterRequest
  ): Promise<AuthResponse> => {
    const response = await api.post<AuthResponse>(
      API_CONFIG.ENDPOINTS.AUTH.REGISTER_RESTAURANT,
      data
    );
    return response.data;
  },

  /**
   * Refresh access token
   */
  refreshToken: async (
    refreshToken: string
  ): Promise<TokenRefreshResponse> => {
    const response = await api.post<TokenRefreshResponse>(
      API_CONFIG.ENDPOINTS.AUTH.REFRESH_TOKEN,
      { refreshToken }
    );
    return response.data;
  },

  /**
   * Logout user
   */
  logout: async (): Promise<ApiResponse> => {
    const response = await api.post<ApiResponse>(
      API_CONFIG.ENDPOINTS.AUTH.LOGOUT
    );
    return response.data;
  },

  /**
   * Get current user profile
   */
  getCurrentUser: async (): Promise<ApiResponse<UserInfo>> => {
    const response = await api.get<ApiResponse<UserInfo>>(
      API_CONFIG.ENDPOINTS.AUTH.ME
    );
    return response.data;
  },

  /**
   * Forgot password - Send reset email
   */
  forgotPassword: async (
    data: ForgotPasswordRequest
  ): Promise<ApiResponse> => {
    const response = await api.post<ApiResponse>(
      API_CONFIG.ENDPOINTS.AUTH.FORGOT_PASSWORD,
      data
    );
    return response.data;
  },

  /**
   * Reset password with token
   */
  resetPassword: async (data: ResetPasswordRequest): Promise<ApiResponse> => {
    const response = await api.post<ApiResponse>(
      API_CONFIG.ENDPOINTS.AUTH.RESET_PASSWORD,
      data
    );
    return response.data;
  },
};