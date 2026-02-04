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
public class PopularItemResponse {
    private Long menuItemId;
    private String itemName;
    private String categoryName;
    private Integer totalQuantitySold;
    private BigDecimal totalRevenue;
    private Integer orderCount;
    private BigDecimal averagePrice;
    private BigDecimal revenueShare;
    private Integer rank;
}
