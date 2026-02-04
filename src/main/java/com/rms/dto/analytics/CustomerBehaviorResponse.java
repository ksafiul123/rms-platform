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
public class CustomerBehaviorResponse {
    private LocalDate analysisDate;
    private Integer totalCustomers;
    private Integer newCustomers;
    private Integer returningCustomers;
    private Integer activeCustomers;
    private Integer dormantCustomers;

    private BigDecimal avgOrdersPerCustomer;
    private BigDecimal avgSpendPerCustomer;
    private BigDecimal avgOrderValue;

    private BigDecimal customerRetentionRate;
    private BigDecimal churnRate;
    private BigDecimal repeatPurchaseRate;

    private Integer peakOrderHour;
    private String preferredOrderType;
    private String preferredPaymentMethod;
}
