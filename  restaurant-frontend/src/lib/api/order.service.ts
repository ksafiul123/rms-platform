import { api } from "./client";
import { API_CONFIG } from "@/config/api.config";
import {
  ApiResponse,
  PaginatedResponse,
  Order,
  CreateOrderRequest,
  UpdateOrderStatusRequest,
  OrderStatus,
} from "@/types";

export const orderService = {
  /**
   * Get all orders with pagination
   */
  getOrders: async (
    restaurantId: number,
    params?: {
      page?: number;
      size?: number;
      status?: OrderStatus;
      orderType?: string;
      startDate?: string;
      endDate?: string;
    }
  ): Promise<PaginatedResponse<Order>> => {
    const response = await api.get<PaginatedResponse<Order>>(
      API_CONFIG.ENDPOINTS.ORDERS.BASE,
      { params: { restaurantId, ...params } }
    );
    return response.data;
  },

  /**
   * Get order by ID
   */
  getOrderById: async (id: number): Promise<ApiResponse<Order>> => {
    const response = await api.get<ApiResponse<Order>>(
      API_CONFIG.ENDPOINTS.ORDERS.BY_ID(id)
    );
    return response.data;
  },

  /**
   * Create new order
   */
  createOrder: async (
    data: CreateOrderRequest,
    restaurantId: number
  ): Promise<ApiResponse<Order>> => {
    const response = await api.post<ApiResponse<Order>>(
      API_CONFIG.ENDPOINTS.ORDERS.BASE,
      data,
      { params: { restaurantId } }
    );
    return response.data;
  },

  /**
   * Update order status
   */
  updateOrderStatus: async (
    id: number,
    data: UpdateOrderStatusRequest
  ): Promise<ApiResponse<Order>> => {
    const response = await api.patch<ApiResponse<Order>>(
      API_CONFIG.ENDPOINTS.ORDERS.STATUS(id),
      data
    );
    return response.data;
  },

  /**
   * Assign order to delivery person
   */
  assignOrder: async (
    id: number,
    deliveryPersonId: number
  ): Promise<ApiResponse<Order>> => {
    const response = await api.patch<ApiResponse<Order>>(
      API_CONFIG.ENDPOINTS.ORDERS.ASSIGN(id),
      { deliveryPersonId }
    );
    return response.data;
  },

  /**
   * Cancel order
   */
  cancelOrder: async (
    id: number,
    reason?: string
  ): Promise<ApiResponse<Order>> => {
    const response = await api.patch<ApiResponse<Order>>(
      API_CONFIG.ENDPOINTS.ORDERS.STATUS(id),
      { status: OrderStatus.CANCELLED, notes: reason }
    );
    return response.data;
  },

  /**
   * Get customer orders
   */
  getCustomerOrders: async (
    customerId: number,
    params?: {
      page?: number;
      size?: number;
    }
  ): Promise<PaginatedResponse<Order>> => {
    const response = await api.get<PaginatedResponse<Order>>(
      API_CONFIG.ENDPOINTS.ORDERS.BASE,
      { params: { customerId, ...params } }
    );
    return response.data;
  },

  /**
   * Get active orders for kitchen
   */
  getActiveOrders: async (
    restaurantId: number
  ): Promise<ApiResponse<Order[]>> => {
    const response = await api.get<ApiResponse<Order[]>>(
      API_CONFIG.ENDPOINTS.ORDERS.BASE,
      {
        params: {
          restaurantId,
          status: [
            OrderStatus.PENDING,
            OrderStatus.CONFIRMED,
            OrderStatus.PREPARING,
          ].join(","),
        },
      }
    );
    return response.data;
  },
};