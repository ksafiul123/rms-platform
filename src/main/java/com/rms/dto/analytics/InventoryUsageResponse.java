package com.rms.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryUsageResponse {
    private Long inventoryItemId;
    private String itemName;
    private String category;
    private BigDecimal quantityUsed;
    private BigDecimal wastageQuantity;
    private String unit;
    private BigDecimal costOfUsage;
    private BigDecimal costOfWastage;
    private BigDecimal usageEfficiency;
}
