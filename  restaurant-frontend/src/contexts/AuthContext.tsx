"use client";

import React, { createContext, useContext, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import {
  AuthContextType,
  LoginRequest,
  RegisterRequest,
  UserInfo,
  RestaurantInfo,
} from "@/types";
import { authService, tokenManager } from "@/lib/api";
import { ROUTES } from "@/config/routes.config";
import { TOKEN_CONFIG } from "@/config/api.config";

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const router = useRouter();
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState<UserInfo | null>(null);
  const [restaurant, setRestaurant] = useState<RestaurantInfo | null>(null);
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [refreshToken, setRefreshToken] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Initialize auth state from localStorage
  useEffect(() => {
    const initAuth = async () => {
      try {
        const token = tokenManager.getAccessToken();
        const refresh = tokenManager.getRefreshToken();

        if (token && refresh) {
          setAccessToken(token);
          setRefreshToken(refresh);

          // Fetch current user
          const response = await authService.getCurrentUser();
          setUser(response.data);
          setIsAuthenticated(true);
        }
      } catch (error) {
        console.error("Auth initialization error:", error);
        // Clear invalid tokens
        tokenManager.clearTokens();
      } finally {
        setLoading(false);
      }
    };

    initAuth();
  }, []);

  // Auto refresh token before expiry
  useEffect(() => {
    if (!refreshToken) return;

    const interval = setInterval(
      async () => {
        try {
          await refreshAccessToken();
        } catch (error) {
          console.error("Auto refresh failed:", error);
        }
      },
      TOKEN_CONFIG.REFRESH_INTERVAL
    );

    return () => clearInterval(interval);
  }, [refreshToken]);

  const login = async (credentials: LoginRequest) => {
    try {
      setLoading(true);
      setError(null);

      const response = await authService.login(credentials);
      const { accessToken, refreshToken, user, restaurant } = response.data;

      // Save tokens
      tokenManager.setTokens(accessToken, refreshToken);

      // Update state
      setAccessToken(accessToken);
      setRefreshToken(refreshToken);
      setUser(user);
      setRestaurant(restaurant || null);
      setIsAuthenticated(true);

      // Redirect to dashboard
      router.push(ROUTES.DASHBOARD);
    } catch (err: any) {
      setError(err.message || "Login failed");
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const register = async (data: RegisterRequest) => {
    try {
      setLoading(true);
      setError(null);

      const response = await authService.register(data);
      const { accessToken, refreshToken, user, restaurant } = response.data;

      // Save tokens
      tokenManager.setTokens(accessToken, refreshToken);

      // Update state
      setAccessToken(accessToken);
      setRefreshToken(refreshToken);
      setUser(user);
      setRestaurant(restaurant || null);
      setIsAuthenticated(true);

      // Redirect to dashboard
      router.push(ROUTES.DASHBOARD);
    } catch (err: any) {
      setError(err.message || "Registration failed");
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const logout = async () => {
    try {
      setLoading(true);
      await authService.logout();
    } catch (error) {
      console.error("Logout error:", error);
    } finally {
      // Clear state
      tokenManager.clearTokens();
      setAccessToken(null);
      setRefreshToken(null);
      setUser(null);
      setRestaurant(null);
      setIsAuthenticated(false);
      setLoading(false);

      // Redirect to login
      router.push(ROUTES.LOGIN);
    }
  };

  const refreshAccessToken = async () => {
    try {
      if (!refreshToken) {
        throw new Error("No refresh token available");
      }

      const response = await authService.refreshToken(refreshToken);
      const { accessToken: newAccessToken, refreshToken: newRefreshToken } =
        response.data;

      // Update tokens
      tokenManager.setTokens(newAccessToken, newRefreshToken);
      setAccessToken(newAccessToken);
      setRefreshToken(newRefreshToken);
    } catch (error) {
      console.error("Token refresh failed:", error);
      await logout();
      throw error;
    }
  };

  const clearError = () => {
    setError(null);
  };

  return (
    <AuthContext.Provider
      value={{
        isAuthenticated,
        user,
        restaurant,
        accessToken,
        refreshToken,
        loading,
        error,
        login,
        register,
        logout,
        refreshAccessToken,
        clearError,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};