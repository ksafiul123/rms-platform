package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@jakarta.persistence.Table(name = "commission_tiers", indexes = {
        @Index(name = "idx_tier_rule", columnList = "commission_rule_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionTier extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commission_rule_id", nullable = false)
    private CommissionRule commissionRule;

    @Column(name = "tier_name", nullable = false, length = 100)
    private String tierName;

    @Column(name = "min_monthly_orders")
    private Integer minMonthlyOrders;

    @Column(name = "max_monthly_orders")
    private Integer maxMonthlyOrders;

    @Column(name = "min_monthly_revenue", precision = 10, scale = 2)
    private BigDecimal minMonthlyRevenue;

    @Column(name = "max_monthly_revenue", precision = 10, scale = 2)
    private BigDecimal maxMonthlyRevenue;

    @Column(name = "commission_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionPercentage;

    @Column(name = "tier_order", nullable = false)
    private Integer tierOrder;
}
