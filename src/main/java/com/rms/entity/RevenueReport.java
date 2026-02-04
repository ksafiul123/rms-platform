package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "revenue_reports", indexes = {
        @Index(name = "idx_revenue_restaurant_date", columnList = "restaurant_id, report_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueReport extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 30)
    private SalesReport.PeriodType periodType;

    // Revenue Breakdown
    @Column(name = "gross_revenue", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal grossRevenue = BigDecimal.ZERO;

    @Column(name = "food_revenue", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal foodRevenue = BigDecimal.ZERO;

    @Column(name = "beverage_revenue", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal beverageRevenue = BigDecimal.ZERO;

    @Column(name = "delivery_charges", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal deliveryCharges = BigDecimal.ZERO;

    @Column(name = "service_charges", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal serviceCharges = BigDecimal.ZERO;

    // Deductions
    @Column(name = "total_discounts", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalDiscounts = BigDecimal.ZERO;

    @Column(name = "total_refunds", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalRefunds = BigDecimal.ZERO;

    @Column(name = "platform_commission", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal platformCommission = BigDecimal.ZERO;

    @Column(name = "payment_gateway_fees", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal paymentGatewayFees = BigDecimal.ZERO;

    // Net Revenue
    @Column(name = "net_revenue", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal netRevenue = BigDecimal.ZERO;

    // Costs
    @Column(name = "cost_of_goods_sold", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal costOfGoodsSold = BigDecimal.ZERO;

    @Column(name = "operating_expenses", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal operatingExpenses = BigDecimal.ZERO;

    @Column(name = "staff_costs", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal staffCosts = BigDecimal.ZERO;

    // Profitability
    @Column(name = "gross_profit", precision = 12, scale = 2)
    private BigDecimal grossProfit;

    @Column(name = "net_profit", precision = 12, scale = 2)
    private BigDecimal netProfit;

    @Column(name = "gross_profit_margin", precision = 5, scale = 2)
    private BigDecimal grossProfitMargin;

    @Column(name = "net_profit_margin", precision = 5, scale = 2)
    private BigDecimal netProfitMargin;

    // Taxes
    @Column(name = "total_taxes", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalTaxes = BigDecimal.ZERO;

    @Column(name = "vat_collected", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal vatCollected = BigDecimal.ZERO;

    @Column(name = "service_tax", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal serviceTax = BigDecimal.ZERO;

    // Comparison
    @Column(name = "previous_period_revenue", precision = 12, scale = 2)
    private BigDecimal previousPeriodRevenue;

    @Column(name = "revenue_growth_percentage", precision = 5, scale = 2)
    private BigDecimal revenueGrowthPercentage;

    @Column(name = "target_revenue", precision = 12, scale = 2)
    private BigDecimal targetRevenue;

    @Column(name = "target_achievement_percentage", precision = 5, scale = 2)
    private BigDecimal targetAchievementPercentage;
}
