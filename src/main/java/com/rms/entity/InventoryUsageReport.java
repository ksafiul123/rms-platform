package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "inventory_usage_reports", indexes = {
        @Index(name = "idx_inventory_usage_restaurant_date", columnList = "restaurant_id, report_date"),
        @Index(name = "idx_inventory_usage_item", columnList = "inventory_item_id, report_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryUsageReport extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_item_id", nullable = false)
    private InventoryItem inventoryItem;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 30)
    private SalesReport.PeriodType periodType;

    // Usage Metrics
    @Column(name = "opening_stock", precision = 10, scale = 3)
    private BigDecimal openingStock;

    @Column(name = "purchases", precision = 10, scale = 3)
    @Builder.Default
    private BigDecimal purchases = BigDecimal.ZERO;

    @Column(name = "total_consumed", precision = 10, scale = 3)
    @Builder.Default
    private BigDecimal totalConsumed = BigDecimal.ZERO;

    @Column(name = "wastage", precision = 10, scale = 3)
    @Builder.Default
    private BigDecimal wastage = BigDecimal.ZERO;

    @Column(name = "closing_stock", precision = 10, scale = 3)
    private BigDecimal closingStock;

    // Cost Metrics
    @Column(name = "total_cost", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalCost = BigDecimal.ZERO;

    @Column(name = "cost_per_unit", precision = 10, scale = 2)
    private BigDecimal costPerUnit;

    @Column(name = "wastage_cost", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal wastageCost = BigDecimal.ZERO;

    // Efficiency Metrics
    @Column(name = "usage_efficiency", precision = 5, scale = 2)
    private BigDecimal usageEfficiency;

    @Column(name = "wastage_percentage", precision = 5, scale = 2)
    private BigDecimal wastagePercentage;

    @Column(name = "stock_turnover_ratio", precision = 5, scale = 2)
    private BigDecimal stockTurnoverRatio;

    @Column(name = "days_to_consume")
    private Integer daysToConsume;

    // Menu Impact
    @Column(name = "dishes_prepared", nullable = false)
    @Builder.Default
    private Integer dishesPrepared = 0;

    @Column(name = "average_per_dish", precision = 10, scale = 3)
    private BigDecimal averagePerDish;

    // Stock Status
    @Column(name = "stock_out_incidents", nullable = false)
    @Builder.Default
    private Integer stockOutIncidents = 0;

    @Column(name = "low_stock_alerts", nullable = false)
    @Builder.Default
    private Integer lowStockAlerts = 0;

    @Column(name = "reorder_triggered", nullable = false)
    @Builder.Default
    private Integer reorderTriggered = 0;

    // Supplier Performance
    @Column(name = "supplier_name", length = 200)
    private String supplierName;

    @Column(name = "purchase_frequency")
    private Integer purchaseFrequency;

    @Column(name = "average_delivery_time_days")
    private Integer averageDeliveryTimeDays;
}
