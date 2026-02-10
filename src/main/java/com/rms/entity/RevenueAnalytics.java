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
@jakarta.persistence.Table(name = "revenue_analytics", indexes = {
        @Index(name = "idx_revenue_analytics_restaurant_date", columnList = "restaurant_id, analysis_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueAnalytics extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false, insertable=false, updatable=false)
    private Restaurant restaurant;

    @Column(name = "analysis_date", nullable = false)
    private LocalDate analysisDate;

    @Column(name = "total_revenue", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(name = "total_cost", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalCost = BigDecimal.ZERO;

    @Column(name = "net_profit", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal netProfit = BigDecimal.ZERO;

    @Column(name = "order_count", nullable = false)
    @Builder.Default
    private Integer orderCount = 0;

    @Column(name = "dine_in_revenue", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal dineInRevenue = BigDecimal.ZERO;

    @Column(name = "takeaway_revenue", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal takeawayRevenue = BigDecimal.ZERO;

    @Column(name = "delivery_revenue", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal deliveryRevenue = BigDecimal.ZERO;

    @Column(name = "online_payment_revenue", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal onlinePaymentRevenue = BigDecimal.ZERO;

    @Column(name = "cash_payment_revenue", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal cashPaymentRevenue = BigDecimal.ZERO;

    @Column(name = "wallet_payment_revenue", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal walletPaymentRevenue = BigDecimal.ZERO;
}
