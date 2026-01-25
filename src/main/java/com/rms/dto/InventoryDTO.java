package com.rms.dto;

//package com.rms.dto;

import com.rms.entity.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class InventoryDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateInventoryItemRequest {

        @NotBlank(message = "Item code is required")
        @Size(max = 50, message = "Item code cannot exceed 50 characters")
        private String itemCode;

        @NotBlank(message = "Item name is required")
        @Size(max = 200, message = "Item name cannot exceed 200 characters")
        private String name;

        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        private String description;

        @NotNull(message = "Category is required")
        private InventoryItem.InventoryCategory category;

        @NotNull(message = "Unit is required")
        private InventoryItem.Unit unit;

        @NotNull(message = "Initial quantity is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Quantity must be positive")
        private BigDecimal initialQuantity;

        @NotNull(message = "Minimum quantity is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Minimum quantity must be positive")
        private BigDecimal minimumQuantity;

        @DecimalMin(value = "0.0", inclusive = true)
        private BigDecimal maximumQuantity;

        @DecimalMin(value = "0.0", inclusive = true)
        private BigDecimal reorderQuantity;

        @NotNull(message = "Cost per unit is required")
        @DecimalMin(value = "0.01", message = "Cost per unit must be greater than 0")
        private BigDecimal costPerUnit;

        @Size(max = 200, message = "Supplier name cannot exceed 200 characters")
        private String supplierName;

        @Size(max = 100, message = "Supplier contact cannot exceed 100 characters")
        private String supplierContact;

        private LocalDate expiryDate;

        @Size(max = 100, message = "Storage location cannot exceed 100 characters")
        private String storageLocation;

        @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
        private String notes;

        private Long branchId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateInventoryItemRequest {

        @Size(max = 200, message = "Item name cannot exceed 200 characters")
        private String name;

        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        private String description;

        private InventoryItem.InventoryCategory category;

        private InventoryItem.Unit unit;

        @DecimalMin(value = "0.0", inclusive = true)
        private BigDecimal minimumQuantity;

        @DecimalMin(value = "0.0", inclusive = true)
        private BigDecimal maximumQuantity;

        @DecimalMin(value = "0.0", inclusive = true)
        private BigDecimal reorderQuantity;

        @DecimalMin(value = "0.01")
        private BigDecimal costPerUnit;

        @Size(max = 200)
        private String supplierName;

        @Size(max = 100)
        private String supplierContact;

        private LocalDate expiryDate;

        @Size(max = 100)
        private String storageLocation;

        @Size(max = 1000)
        private String notes;

        private Boolean isActive;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddStockRequest {

        @NotNull(message = "Quantity is required")
        @DecimalMin(value = "0.001", message = "Quantity must be greater than 0")
        private BigDecimal quantity;

        @NotNull(message = "Transaction type is required")
        private StockTransaction.TransactionType transactionType;

        @DecimalMin(value = "0.01")
        private BigDecimal costPerUnit;

        @Size(max = 100)
        private String referenceNumber;

        @Size(max = 200)
        private String supplier;

        @Size(max = 1000)
        private String notes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeductStockRequest {

        @NotNull(message = "Quantity is required")
        @DecimalMin(value = "0.001", message = "Quantity must be greater than 0")
        private BigDecimal quantity;

        @NotNull(message = "Transaction type is required")
        private StockTransaction.TransactionType transactionType;

        @Size(max = 1000)
        private String notes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LinkMenuItemRequest {

        @NotNull(message = "Menu item ID is required")
        private Long menuItemId;

        @NotNull(message = "Quantity required is required")
        @DecimalMin(value = "0.001", message = "Quantity must be greater than 0")
        private BigDecimal quantityRequired;

        private Boolean isOptional = false;

        @Size(max = 500)
        private String notes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkLinkMenuItemRequest {

        @NotEmpty(message = "Inventory items list cannot be empty")
        @Valid
        private List<InventoryLinkItem> inventoryItems;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryLinkItem {

        @NotNull(message = "Inventory item ID is required")
        private Long inventoryItemId;

        @NotNull(message = "Quantity required is required")
        @DecimalMin(value = "0.001")
        private BigDecimal quantityRequired;

        private Boolean isOptional = false;

        @Size(max = 500)
        private String notes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryItemResponse {

        private Long id;
        private Long restaurantId;
        private Long branchId;
        private String itemCode;
        private String name;
        private String description;
        private InventoryItem.InventoryCategory category;
        private InventoryItem.Unit unit;
        private BigDecimal currentQuantity;
        private BigDecimal minimumQuantity;
        private BigDecimal maximumQuantity;
        private BigDecimal reorderQuantity;
        private BigDecimal costPerUnit;
        private BigDecimal totalValue;
        private String supplierName;
        private String supplierContact;
        private InventoryItem.InventoryStatus status;
        private LocalDate expiryDate;
        private String storageLocation;
        private String notes;
        private Boolean isActive;
        private Boolean isLowStock;
        private Boolean isOutOfStock;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockTransactionResponse {

        private Long id;
        private Long inventoryItemId;
        private String inventoryItemName;
        private StockTransaction.TransactionType transactionType;
        private BigDecimal quantity;
        private BigDecimal quantityBefore;
        private BigDecimal quantityAfter;
        private BigDecimal costPerUnit;
        private BigDecimal totalCost;
        private Long orderId;
        private String orderNumber;
        private String performedByName;
        private String referenceNumber;
        private String supplier;
        private String notes;
        private LocalDateTime createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuItemInventoryResponse {

        private Long id;
        private Long menuItemId;
        private String menuItemName;
        private Long inventoryItemId;
        private String inventoryItemName;
        private BigDecimal quantityRequired;
        private InventoryItem.Unit unit;
        private Boolean isOptional;
        private BigDecimal currentStock;
        private Boolean isAvailable;
        private String notes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LowStockAlertResponse {

        private Long id;
        private Long inventoryItemId;
        private String inventoryItemName;
        private String itemCode;
        private LowStockAlert.AlertType alertType;
        private BigDecimal currentQuantity;
        private BigDecimal minimumQuantity;
        private InventoryItem.Unit unit;
        private LowStockAlert.AlertStatus status;
        private String acknowledgedByName;
        private LocalDateTime acknowledgedAt;
        private LocalDateTime resolvedAt;
        private String notes;
        private LocalDateTime createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryReportResponse {

        private Long totalItems;
        private Long inStockItems;
        private Long lowStockItems;
        private Long outOfStockItems;
        private BigDecimal totalInventoryValue;
        private List<CategorySummary> categorySummaries;
        private List<InventoryItemResponse> topValueItems;
        private List<InventoryItemResponse> recentlyUpdated;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySummary {

        private InventoryItem.InventoryCategory category;
        private Long itemCount;
        private BigDecimal totalValue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockAvailabilityResponse {

        private Long menuItemId;
        private String menuItemName;
        private Boolean isAvailable;
        private List<IngredientAvailability> ingredients;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IngredientAvailability {

        private Long inventoryItemId;
        private String inventoryItemName;
        private BigDecimal requiredQuantity;
        private BigDecimal availableQuantity;
        private Boolean isAvailable;
        private Boolean isOptional;
    }
}
