package com.rms.dto.analytics;

//package com.rms.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

// ==================== Sales Report DTOs ====================

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailySalesReportResponse {
    private LocalDate reportDate;
    private Integer totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal totalCost;
    private BigDecimal netProfit;
    private BigDecimal averageOrderValue;
    private BigDecimal totalDiscounts;
    private BigDecimal totalTaxes;

    // By order type
    private Integer dineInOrders;
    private BigDecimal dineInRevenue;
    private Integer takeawayOrders;
    private BigDecimal takeawayRevenue;
    private Integer deliveryOrders;
    private BigDecimal deliveryRevenue;

    // By payment method
    private BigDecimal onlinePayments;
    private BigDecimal cashPayments;
    private BigDecimal walletPayments;

    // Customer metrics
    private Integer uniqueCustomers;
    private Integer newCustomers;
    private Integer returningCustomers;

    // Negative metrics
    private Integer cancelledOrders;
    private BigDecimal refundedAmount;
}

