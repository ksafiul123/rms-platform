package com.rms.entity;

//package com.rms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Tracks low stock alerts for inventory items
 */
@Entity
@jakarta.persistence.Table(name = "low_stock_alerts",
        indexes = {
                @Index(name = "idx_low_stock_inventory", columnList = "inventory_item_id"),
                @Index(name = "idx_low_stock_restaurant", columnList = "restaurant_id"),
                @Index(name = "idx_low_stock_status", columnList = "status"),
                @Index(name = "idx_low_stock_created", columnList = "created_at")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LowStockAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_item_id", nullable = false)
    private InventoryItem inventoryItem;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false, length = 20)
    private AlertType alertType;

    @Column(name = "current_quantity", nullable = false, precision = 10, scale = 3)
    private BigDecimal currentQuantity;

    @Column(name = "minimum_quantity", nullable = false, precision = 10, scale = 3)
    private BigDecimal minimumQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AlertStatus status;

    @Column(name = "acknowledged_by")
    private Long acknowledgedBy;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum AlertType {
        LOW_STOCK,      // Below minimum quantity
        OUT_OF_STOCK,   // Zero quantity
        EXPIRING_SOON   // Approaching expiry date
    }

    public enum AlertStatus {
        ACTIVE,         // Alert is active
        ACKNOWLEDGED,   // Someone has seen the alert
        RESOLVED        // Issue has been resolved
    }

    public void acknowledge(Long userId) {
        this.status = AlertStatus.ACKNOWLEDGED;
        this.acknowledgedBy = userId;
        this.acknowledgedAt = LocalDateTime.now();
    }

    public void resolve() {
        this.status = AlertStatus.RESOLVED;
        this.resolvedAt = LocalDateTime.now();
    }
}
