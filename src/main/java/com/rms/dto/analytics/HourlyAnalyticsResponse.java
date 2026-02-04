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
public class HourlyAnalyticsResponse {
    private LocalDate date;
    private List<HourlyBreakdown> hourlyData;
    private Integer peakHour;
    private Integer slowestHour;
    private BigDecimal peakHourRevenue;
}
