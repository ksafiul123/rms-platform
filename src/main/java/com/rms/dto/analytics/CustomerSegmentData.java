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
public class CustomerSegmentData {
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private Integer totalOrders;
    private BigDecimal totalSpent;
    private BigDecimal avgOrderValue;
    private LocalDate lastOrderDate;
    private Long daysSinceLastOrder;
    private CustomerSegment segment;
}
