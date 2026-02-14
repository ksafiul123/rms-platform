import { OrderStatus, OrderType, PaymentStatus } from "@/types";

// Order Status Display
export const ORDER_STATUS_DISPLAY: Record<OrderStatus, string> = {
  [OrderStatus.PENDING]: "Pending",
  [OrderStatus.CONFIRMED]: "Confirmed",
  [OrderStatus.PREPARING]: "Preparing",
  [OrderStatus.READY]: "Ready",
  [OrderStatus.OUT_FOR_DELIVERY]: "Out for Delivery",
  [OrderStatus.DELIVERED]: "Delivered",
  [OrderStatus.COMPLETED]: "Completed",
  [OrderStatus.CANCELLED]: "Cancelled",
};

// Order Status Colors
export const ORDER_STATUS_COLORS: Record<OrderStatus, string> = {
  [OrderStatus.PENDING]: "bg-yellow-100 text-yellow-800",
  [OrderStatus.CONFIRMED]: "bg-blue-100 text-blue-800",
  [OrderStatus.PREPARING]: "bg-purple-100 text-purple-800",
  [OrderStatus.READY]: "bg-green-100 text-green-800",
  [OrderStatus.OUT_FOR_DELIVERY]: "bg-indigo-100 text-indigo-800",
  [OrderStatus.DELIVERED]: "bg-teal-100 text-teal-800",
  [OrderStatus.COMPLETED]: "bg-gray-100 text-gray-800",
  [OrderStatus.CANCELLED]: "bg-red-100 text-red-800",
};

// Order Type Display
export const ORDER_TYPE_DISPLAY: Record<OrderType, string> = {
  [OrderType.DINE_IN]: "Dine In",
  [OrderType.TAKEAWAY]: "Takeaway",
  [OrderType.DELIVERY]: "Delivery",
};

// Payment Status Display
export const PAYMENT_STATUS_DISPLAY: Record<PaymentStatus, string> = {
  [PaymentStatus.PENDING]: "Pending",
  [PaymentStatus.PAID]: "Paid",
  [PaymentStatus.PARTIALLY_PAID]: "Partially Paid",
  [PaymentStatus.REFUNDED]: "Refunded",
  [PaymentStatus.FAILED]: "Failed",
};

// Payment Status Colors
export const PAYMENT_STATUS_COLORS: Record<PaymentStatus, string> = {
  [PaymentStatus.PENDING]: "bg-yellow-100 text-yellow-800",
  [PaymentStatus.PAID]: "bg-green-100 text-green-800",
  [PaymentStatus.PARTIALLY_PAID]: "bg-orange-100 text-orange-800",
  [PaymentStatus.REFUNDED]: "bg-purple-100 text-purple-800",
  [PaymentStatus.FAILED]: "bg-red-100 text-red-800",
};

// Pagination
export const DEFAULT_PAGE_SIZE = 10;
export const PAGE_SIZE_OPTIONS = [10, 25, 50, 100];

// Date Formats
export const DATE_FORMAT = "MMM dd, yyyy";
export const DATE_TIME_FORMAT = "MMM dd, yyyy HH:mm";
export const TIME_FORMAT = "HH:mm";

// Validation
export const VALIDATION_RULES = {
  EMAIL: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
  PHONE: /^[+]?[(]?[0-9]{1,4}[)]?[-\s.]?[(]?[0-9]{1,4}[)]?[-\s.]?[0-9]{1,9}$/,
  PASSWORD_MIN_LENGTH: 8,
  NAME_MIN_LENGTH: 2,
  NAME_MAX_LENGTH: 100,
};

// File Upload
export const MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
export const ALLOWED_IMAGE_TYPES = ["image/jpeg", "image/png", "image/webp"];

// Toast Messages
export const TOAST_DURATION = 3000;