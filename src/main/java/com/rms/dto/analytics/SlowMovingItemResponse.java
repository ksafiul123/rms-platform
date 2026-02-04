package com.rms.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlowMovingItemResponse {
    private Long inventoryItemId;
    private String itemName;
    private String category;
    private BigDecimal currentQuantity;
    private String unit;
    private LocalDate lastUsedDate;
    private Long daysSinceLastUse;
    private BigDecimal estimatedValue;
    private String recommendation;
}
