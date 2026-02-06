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
 * Tracks all inventory stock movements (in/out)
 */
@Entity
@jakarta.persistence.Table(name = "stock_transactions",
        indexes = {
                @Index(name = "idx_stock_transaction_inventory", columnList = "inventory_item_id"),
                @Index(name = "idx_stock_transaction_type", columnList = "transaction_type"),
                @Index(name = "idx_stock_transaction_order", columnList = "order_id"),
                @Index(name = "idx_stock_transaction_created", columnList = "created_at")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_item_id", nullable = false)
    private InventoryItem inventoryItem;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    private TransactionType transactionType;

    @Column(name = "quantity", nullable = false, precision = 10, scale = 3)
    private BigDecimal quantity; // Positive for IN, Negative for OUT

    @Column(name = "quantity_before", nullable = false, precision = 10, scale = 3)
    private BigDecimal quantityBefore;

    @Column(name = "quantity_after", nullable = false, precision = 10, scale = 3)
    private BigDecimal quantityAfter;

    @Column(name = "cost_per_unit", precision = 10, scale = 2)
    private BigDecimal costPerUnit;

    @Column(name = "total_cost", precision = 10, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "order_id")
    private Long orderId; // Reference to order if deduction was due to order

    @Column(name = "performed_by", nullable = false)
    private Long performedBy;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber; // PO number, invoice number, etc.

    @Column(name = "supplier", length = 200)
    private String supplier;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum TransactionType {
        PURCHASE,           // Stock received from supplier
        MANUAL_ADDITION,    // Manual stock increase
        ORDER_DEDUCTION,    // Stock used for order
        WASTAGE,            // Stock wasted/spoiled
        MANUAL_DEDUCTION,   // Manual stock decrease
        ADJUSTMENT,         // Stock count adjustment
        TRANSFER_IN,        // Transfer from another branch
        TRANSFER_OUT,       // Transfer to another branch
        RETURN_TO_SUPPLIER  // Returned to supplier
    }

    public boolean isStockIncrease() {
        return quantity.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isStockDecrease() {
        return quantity.compareTo(BigDecimal.ZERO) < 0;
    }
}
