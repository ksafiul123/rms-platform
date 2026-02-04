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
public class HourlyBreakdown {
    private Integer hour;
    private Integer orderCount;
    private BigDecimal revenue;
    private Integer customerCount;
    private BigDecimal avgOrderValue;
}
