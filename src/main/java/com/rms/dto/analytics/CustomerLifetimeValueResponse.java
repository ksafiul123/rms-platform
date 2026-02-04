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
public class CustomerLifetimeValueResponse {
    private Long customerId;
    private String customerName;
    private LocalDate registeredDate;
    private Integer totalOrders;
    private BigDecimal totalSpent;
    private BigDecimal averageOrderValue;
    private Long daysSinceFirstOrder;
    private Double orderFrequency;
    private BigDecimal predictedLifetimeValue;
    private CustomerSegment customerSegment;
}
