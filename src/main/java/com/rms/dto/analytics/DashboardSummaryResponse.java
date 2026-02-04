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
public class DashboardSummaryResponse {
    private LocalDate date;

    // Today's metrics
    private BigDecimal todayRevenue;
    private Integer todayOrders;
    private BigDecimal todayProfit;
    private BigDecimal avgOrderValue;

    // Comparisons
    private BigDecimal revenueVsYesterday;
    private BigDecimal revenueVsLastWeek;
    private BigDecimal revenueVsLastMonth;

    // Current status
    private Integer activeOrders;
    private Integer pendingOrders;
    private Integer completedToday;

    // Top performers
    private List<PopularItemResponse> topItems;
    private List<CustomerSegmentData> topCustomers;

    // Alerts
    private Integer lowStockAlerts;
    private Integer outOfStockItems;
    private Integer overdueOrders;
}
