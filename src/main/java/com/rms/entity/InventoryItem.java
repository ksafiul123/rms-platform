package com.rms.entity;

//package com.rms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@jakarta.persistence.Table(name = "inventory_items",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"restaurant_id", "item_code"})
        },
        indexes = {
                @Index(name = "idx_inventory_restaurant", columnList = "restaurant_id"),
                @Index(name = "idx_inventory_category", columnList = "category"),
                @Index(name = "idx_inventory_status", columnList = "status"),
                @Index(name = "idx_inventory_item_code", columnList = "item_code")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "item_code", nullable = false, length = 50)
    private String itemCode; // SKU or unique identifier

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private InventoryCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit", nullable = false, length = 20)
    private Unit unit;

    @Column(name = "current_quantity", nullable = false, precision = 10, scale = 3)
    private BigDecimal currentQuantity;

    @Column(name = "minimum_quantity", nullable = false, precision = 10, scale = 3)
    private BigDecimal minimumQuantity; // Reorder point

    @Column(name = "maximum_quantity", precision = 10, scale = 3)
    private BigDecimal maximumQuantity; // Max stock level

    @Column(name = "reorder_quantity", precision = 10, scale = 3)
    private BigDecimal reorderQuantity; // Standard reorder amount

    @Column(name = "cost_per_unit", nullable = false, precision = 10, scale = 2)
    private BigDecimal costPerUnit;

    @Column(name = "supplier_name", length = 200)
    private String supplierName;

    @Column(name = "supplier_contact", length = 100)
    private String supplierContact;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InventoryStatus status;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "storage_location", length = 100)
    private String storageLocation;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "inventoryItem", cascade = CascadeType.ALL)
    private List<MenuItemInventory> menuItemLinks = new ArrayList<>();

    @OneToMany(mappedBy = "inventoryItem", cascade = CascadeType.ALL)
    private List<StockTransaction> transactions = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum InventoryCategory {
        VEGETABLES,
        FRUITS,
        MEAT,
        SEAFOOD,
        DAIRY,
        GRAINS,
        SPICES,
        BEVERAGES,
        CONDIMENTS,
        PACKAGING,
        CLEANING_SUPPLIES,
        OTHER
    }

    public enum Unit {
        KG,          // Kilogram
        G,           // Gram
        L,           // Liter
        ML,          // Milliliter
        PCS,         // Pieces
        DOZEN,       // Dozen
        BOX,         // Box
        PACKET,      // Packet
        BOTTLE,      // Bottle
        CAN          // Can
    }

    public enum InventoryStatus {
        IN_STOCK,
        LOW_STOCK,
        OUT_OF_STOCK,
        DISCONTINUED
    }

    // Helper methods
    public void addTransaction(StockTransaction transaction) {
        transactions.add(transaction);
        transaction.setInventoryItem(this);
    }

    public boolean isLowStock() {
        return currentQuantity.compareTo(minimumQuantity) <= 0;
    }

    public boolean isOutOfStock() {
        return currentQuantity.compareTo(BigDecimal.ZERO) <= 0;
    }

    public void updateStatus() {
        if (isOutOfStock()) {
            this.status = InventoryStatus.OUT_OF_STOCK;
        } else if (isLowStock()) {
            this.status = InventoryStatus.LOW_STOCK;
        } else {
            this.status = InventoryStatus.IN_STOCK;
        }
    }

    public BigDecimal calculateTotalValue() {
        return currentQuantity.multiply(costPerUnit);
    }

    public BigDecimal getMinimumQuantity() {
        return minimumQuantity;
    }

    public BigDecimal getCurrentQuantity() {
        return currentQuantity;
    }
}
