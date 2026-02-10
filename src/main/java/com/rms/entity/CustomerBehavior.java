package com.rms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@jakarta.persistence.Table(name = "customer_behavior", indexes = {
        @Index(name = "idx_customer_behavior_restaurant_date", columnList = "restaurant_id, analysis_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerBehavior extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false, insertable=false, updatable=false)
    private Restaurant restaurant;

    @Column(name = "analysis_date", nullable = false)
    private LocalDate analysisDate;

    @Column(name = "total_customers", nullable = false)
    @Builder.Default
    private Integer totalCustomers = 0;

    @Column(name = "new_customers", nullable = false)
    @Builder.Default
    private Integer newCustomers = 0;

    @Column(name = "returning_customers", nullable = false)
    @Builder.Default
    private Integer returningCustomers = 0;

    @Column(name = "avg_orders_per_customer", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal avgOrdersPerCustomer = BigDecimal.ZERO;

    @Column(name = "avg_spend_per_customer", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal avgSpendPerCustomer = BigDecimal.ZERO;

    @Column(name = "customer_retention_rate", precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal customerRetentionRate = BigDecimal.ZERO;

    @Column(name = "churn_rate", precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal churnRate = BigDecimal.ZERO;
}
