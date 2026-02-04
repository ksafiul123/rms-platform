package com.rms.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryEfficiencyResponse {
    private LocalDate analysisDate;
    private Integer totalItems;
    private Integer lowStockItems;
    private Integer outOfStockItems;
    private Integer overstockItems;
    private BigDecimal totalInventoryValue;
    private BigDecimal totalQuantityUsed;
    private BigDecimal totalWastage;
    private BigDecimal wastagePercentage;
    private BigDecimal stockHealthScore;
    private List<String> recommendations;
}
