package com.rms.dto.order;

//package com.rms.dto;

import com.rms.entity.Order;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateOrderRequest {

        @NotNull(message = "Order type is required")
        private Order.OrderType orderType;

        @Size(max = 10, message = "Table number cannot exceed 10 characters")
        private String tableNumber; // Required for DINE_IN

        @Size(max = 500, message = "Delivery address cannot exceed 500 characters")
        private String deliveryAddress; // Required for DELIVERY

        @NotEmpty(message = "Order items cannot be empty")
        @Valid
        private List<OrderItemRequest> items;

        @Size(max = 1000, message = "Special instructions cannot exceed 1000 characters")
        private String specialInstructions;

        @DecimalMin(value = "0.0", inclusive = true, message = "Discount amount must be positive")
        private BigDecimal discountAmount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {

        @NotNull(message = "Menu item ID is required")
        private Long menuItemId;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;

        @Size(max = 500, message = "Special instructions cannot exceed 500 characters")
        private String specialInstructions;

        private List<Long> modifierIds;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateOrderStatusRequest {

        @NotNull(message = "Status is required")
        private Order.OrderStatus status;

        @Size(max = 500, message = "Notes cannot exceed 500 characters")
        private String notes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CancelOrderRequest {

        @NotBlank(message = "Cancellation reason is required")
        @Size(max = 1000, message = "Cancellation reason cannot exceed 1000 characters")
        private String reason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignDeliveryManRequest {

        @NotNull(message = "Delivery man ID is required")
        private Long deliveryManId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderResponse {

        private Long id;
        private Long restaurantId;
        private Long customerId;
        private String customerName;
        private String orderNumber;
        private Order.OrderType orderType;
        private Order.OrderStatus status;
        private String tableNumber;
        private String deliveryAddress;
        private Long deliveryManId;
        private String deliveryManName;
        private BigDecimal subtotal;
        private BigDecimal taxAmount;
        private BigDecimal deliveryFee;
        private BigDecimal discountAmount;
        private BigDecimal totalAmount;
        private String specialInstructions;
        private LocalDateTime estimatedReadyTime;
        private LocalDateTime actualReadyTime;
        private LocalDateTime deliveryTime;
        private List<OrderItemResponse> items;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {

        private Long id;
        private Long menuItemId;
        private String itemName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        private String specialInstructions;
        private List<OrderItemModifierResponse> modifiers;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemModifierResponse {

        private Long id;
        private Long modifierId;
        private String modifierName;
        private BigDecimal price;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderSummaryResponse {

        private Long id;
        private String orderNumber;
        private Order.OrderType orderType;
        private Order.OrderStatus status;
        private BigDecimal totalAmount;
        private Integer itemCount;
        private LocalDateTime createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderStatusHistoryResponse {

        private Long id;
        private Order.OrderStatus fromStatus;
        private Order.OrderStatus toStatus;
        private String changedByName;
        private String notes;
        private LocalDateTime createdAt;
    }
}
