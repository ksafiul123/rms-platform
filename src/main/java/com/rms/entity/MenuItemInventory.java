package com.rms.entity;

//package com.rms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Links menu items to inventory items with quantity requirements
 * Defines how much of each inventory item is needed per menu item
 */
@Entity
@jakarta.persistence.Table(name = "menu_item_inventory",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"menu_item_id", "inventory_item_id"})
        },
        indexes = {
                @Index(name = "idx_menu_item_inv_menu", columnList = "menu_item_id"),
                @Index(name = "idx_menu_item_inv_inventory", columnList = "inventory_item_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_item_id", nullable = false)
    private InventoryItem inventoryItem;

    @Column(name = "quantity_required", nullable = false, precision = 10, scale = 3)
    private BigDecimal quantityRequired; // Amount needed per 1 menu item

    @Column(name = "is_optional", nullable = false)
    private Boolean isOptional = false; // If true, menu item can be made without this ingredient

    @Column(name = "notes", length = 500)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Calculate total quantity needed for given number of menu items
     */
    public BigDecimal calculateRequiredQuantity(int menuItemCount) {
        return quantityRequired.multiply(new BigDecimal(menuItemCount));
    }
}
