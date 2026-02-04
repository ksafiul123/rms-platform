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
public class ItemPerformanceTrendResponse {
    private LocalDate date;
    private Integer quantitySold;
    private BigDecimal revenue;
    private Integer orderCount;
    private BigDecimal averagePrice;
}
