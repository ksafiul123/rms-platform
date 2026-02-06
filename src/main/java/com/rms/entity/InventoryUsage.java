package com.rms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@jakarta.persistence.Table(name = "inventory_usage", indexes = {
        @Index(name = "idx_inventory_usage_restaurant_date", columnList = "restaurant_id, analysis_date"),
        @Index(name = "idx_inventory_usage_item_date", columnList = "inventory_item_id, analysis_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryUsage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_item_id", nullable = false)
    private InventoryItem inventoryItem;

    @Column(name = "analysis_date", nullable = false)
    private LocalDate analysisDate;

    @Column(name = "quantity_used", precision = 10, scale = 3)
    @Builder.Default
    private BigDecimal quantityUsed = BigDecimal.ZERO;

    @Column(name = "wastage_quantity", precision = 10, scale = 3)
    @Builder.Default
    private BigDecimal wastageQuantity = BigDecimal.ZERO;

    @Column(name = "cost_of_usage", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal costOfUsage = BigDecimal.ZERO;

    @Column(name = "cost_of_wastage", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal costOfWastage = BigDecimal.ZERO;
}
