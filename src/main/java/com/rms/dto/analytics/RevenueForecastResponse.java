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
public class RevenueForecastResponse {
    private LocalDate forecastStartDate;
    private LocalDate forecastEndDate;
    private BigDecimal historicalAverage;
    private String trend;
    private List<RevenueForecastDay> forecast;
    private BigDecimal totalProjectedRevenue;
}
