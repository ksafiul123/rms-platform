import { api } from "./client";
import { API_CONFIG } from "@/config/api.config";
import {
  ApiResponse,
  PaginatedResponse,
  MenuCategory,
  MenuItem,
  ModifierGroup,
  MenuCategoryRequest,
  MenuItemRequest,
  ModifierGroupRequest,
} from "@/types";

export const menuService = {
  // ==================== Categories ====================

  /**
   * Get all categories for a restaurant
   */
  getCategories: async (
    restaurantId: number
  ): Promise<ApiResponse<MenuCategory[]>> => {
    const response = await api.get<ApiResponse<MenuCategory[]>>(
      API_CONFIG.ENDPOINTS.MENU.CATEGORIES,
      { params: { restaurantId } }
    );
    return response.data;
  },

  /**
   * Get category by ID
   */
  getCategoryById: async (id: number): Promise<ApiResponse<MenuCategory>> => {
    const response = await api.get<ApiResponse<MenuCategory>>(
      API_CONFIG.ENDPOINTS.MENU.CATEGORY_BY_ID(id)
    );
    return response.data;
  },

  /**
   * Create new category
   */
  createCategory: async (
    data: MenuCategoryRequest,
    restaurantId: number
  ): Promise<ApiResponse<MenuCategory>> => {
    const response = await api.post<ApiResponse<MenuCategory>>(
      API_CONFIG.ENDPOINTS.MENU.CATEGORIES,
      data,
      { params: { restaurantId } }
    );
    return response.data;
  },

  /**
   * Update category
   */
  updateCategory: async (
    id: number,
    data: MenuCategoryRequest
  ): Promise<ApiResponse<MenuCategory>> => {
    const response = await api.put<ApiResponse<MenuCategory>>(
      API_CONFIG.ENDPOINTS.MENU.CATEGORY_BY_ID(id),
      data
    );
    return response.data;
  },

  /**
   * Delete category
   */
  deleteCategory: async (id: number): Promise<ApiResponse> => {
    const response = await api.delete<ApiResponse>(
      API_CONFIG.ENDPOINTS.MENU.CATEGORY_BY_ID(id)
    );
    return response.data;
  },

  // ==================== Menu Items ====================

  /**
   * Get all menu items
   */
  getMenuItems: async (
    restaurantId: number,
    params?: {
      categoryId?: number;
      page?: number;
      size?: number;
      search?: string;
    }
  ): Promise<PaginatedResponse<MenuItem>> => {
    const response = await api.get<PaginatedResponse<MenuItem>>(
      API_CONFIG.ENDPOINTS.MENU.ITEMS,
      { params: { restaurantId, ...params } }
    );
    return response.data;
  },

  /**
   * Get menu item by ID
   */
  getMenuItemById: async (id: number): Promise<ApiResponse<MenuItem>> => {
    const response = await api.get<ApiResponse<MenuItem>>(
      API_CONFIG.ENDPOINTS.MENU.ITEM_BY_ID(id)
    );
    return response.data;
  },

  /**
   * Create new menu item
   */
  createMenuItem: async (
    data: MenuItemRequest,
    restaurantId: number
  ): Promise<ApiResponse<MenuItem>> => {
    const response = await api.post<ApiResponse<MenuItem>>(
      API_CONFIG.ENDPOINTS.MENU.ITEMS,
      data,
      { params: { restaurantId } }
    );
    return response.data;
  },

  /**
   * Update menu item
   */
  updateMenuItem: async (
    id: number,
    data: MenuItemRequest
  ): Promise<ApiResponse<MenuItem>> => {
    const response = await api.put<ApiResponse<MenuItem>>(
      API_CONFIG.ENDPOINTS.MENU.ITEM_BY_ID(id),
      data
    );
    return response.data;
  },

  /**
   * Delete menu item
   */
  deleteMenuItem: async (id: number): Promise<ApiResponse> => {
    const response = await api.delete<ApiResponse>(
      API_CONFIG.ENDPOINTS.MENU.ITEM_BY_ID(id)
    );
    return response.data;
  },

  /**
   * Toggle item availability
   */
  toggleItemAvailability: async (
    id: number,
    isAvailable: boolean
  ): Promise<ApiResponse<MenuItem>> => {
    const response = await api.patch<ApiResponse<MenuItem>>(
      `${API_CONFIG.ENDPOINTS.MENU.ITEM_BY_ID(id)}/availability`,
      { isAvailable }
    );
    return response.data;
  },

  // ==================== Modifier Groups ====================

  /**
   * Get all modifier groups
   */
  getModifierGroups: async (
    restaurantId: number
  ): Promise<ApiResponse<ModifierGroup[]>> => {
    const response = await api.get<ApiResponse<ModifierGroup[]>>(
      API_CONFIG.ENDPOINTS.MENU.MODIFIERS,
      { params: { restaurantId } }
    );
    return response.data;
  },

  /**
   * Create modifier group
   */
  createModifierGroup: async (
    data: ModifierGroupRequest,
    restaurantId: number
  ): Promise<ApiResponse<ModifierGroup>> => {
    const response = await api.post<ApiResponse<ModifierGroup>>(
      API_CONFIG.ENDPOINTS.MENU.MODIFIERS,
      data,
      { params: { restaurantId } }
    );
    return response.data;
  },

  /**
   * Update modifier group
   */
  updateModifierGroup: async (
    id: number,
    data: ModifierGroupRequest
  ): Promise<ApiResponse<ModifierGroup>> => {
    const response = await api.put<ApiResponse<ModifierGroup>>(
      API_CONFIG.ENDPOINTS.MENU.MODIFIER_BY_ID(id),
      data
    );
    return response.data;
  },

  /**
   * Delete modifier group
   */
  deleteModifierGroup: async (id: number): Promise<ApiResponse> => {
    const response = await api.delete<ApiResponse>(
      API_CONFIG.ENDPOINTS.MENU.MODIFIER_BY_ID(id)
    );
    return response.data;
  },

  // ==================== Public Menu ====================

  /**
   * Get public menu (no authentication required)
   */
  getPublicMenu: async (
    restaurantId: number
  ): Promise<ApiResponse<MenuItem[]>> => {
    const response = await api.get<ApiResponse<MenuItem[]>>(
      API_CONFIG.ENDPOINTS.MENU.PUBLIC,
      { params: { restaurantId } }
    );
    return response.data;
  },
};