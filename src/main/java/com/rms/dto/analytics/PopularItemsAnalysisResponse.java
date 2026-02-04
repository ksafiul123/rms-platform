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
public class PopularItemsAnalysisResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalItems;
    private BigDecimal totalRevenue;
    private List<PopularItemResponse> topByRevenue;
    private List<PopularItemResponse> topByQuantity;
    private BigDecimal revenueConcentration;
}
