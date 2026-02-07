package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@jakarta.persistence.Table(name = "sales_reports", indexes = {
        @Index(name = "idx_sales_restaurant_date", columnList = "restaurant_id, report_date"),
        @Index(name = "idx_sales_date", columnList = "report_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesReport extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 30)
    private PeriodType periodType;

    // Sales Metrics
    @Column(name = "total_orders", nullable = false)
    @Builder.Default
    private Integer totalOrders = 0;

    @Column(name = "total_revenue", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(name = "total_cost", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalCost = BigDecimal.ZERO;

    @Column(name = "net_profit", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal netProfit = BigDecimal.ZERO;

    @Column(name = "total_discounts", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalDiscounts = BigDecimal.ZERO;

    @Column(name = "total_taxes", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalTaxes = BigDecimal.ZERO;

    @Column(name = "net_revenue", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal netRevenue = BigDecimal.ZERO;

    @Column(name = "total_tips", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalTips = BigDecimal.ZERO;

    // Order Breakdown
    @Column(name = "dine_in_orders", nullable = false)
    @Builder.Default
    private Integer dineInOrders = 0;

    @Column(name = "dine_in_revenue", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal dineInRevenue = BigDecimal.ZERO;

    @Column(name = "takeaway_orders", nullable = false)
    @Builder.Default
    private Integer takeawayOrders = 0;

    @Column(name = "takeaway_revenue", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal takeawayRevenue = BigDecimal.ZERO;

    @Column(name = "delivery_orders", nullable = false)
    @Builder.Default
    private Integer deliveryOrders = 0;

    @Column(name = "delivery_revenue", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal deliveryRevenue = BigDecimal.ZERO;

    // Customer Metrics
    @Column(name = "new_customers", nullable = false)
    @Builder.Default
    private Integer newCustomers = 0;

    @Column(name = "returning_customers", nullable = false)
    @Builder.Default
    private Integer returningCustomers = 0;

    @Column(name = "unique_customers", nullable = false)
    @Builder.Default
    private Integer uniqueCustomers = 0;

    // Performance Metrics
    @Column(name = "average_order_value", precision = 10, scale = 2)
    private BigDecimal averageOrderValue;

    @Column(name = "average_items_per_order", precision = 5, scale = 2)
    private BigDecimal averageItemsPerOrder;

    @Column(name = "order_completion_rate", precision = 5, scale = 2)
    private BigDecimal orderCompletionRate;

    @Column(name = "cancelled_orders", nullable = false)
    @Builder.Default
    private Integer cancelledOrders = 0;

    @Column(name = "refunded_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal refundedAmount = BigDecimal.ZERO;

    // Payment Methods
    @Column(name = "cash_payments", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal cashPayments = BigDecimal.ZERO;

    @Column(name = "card_payments", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal cardPayments = BigDecimal.ZERO;

    @Column(name = "online_payments", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal onlinePayments = BigDecimal.ZERO;

    @Column(name = "wallet_payments", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal walletPayments = BigDecimal.ZERO;

    // Time-based Analysis
    @Column(name = "peak_hour", length = 5)
    private String peakHour;

    @Column(name = "peak_hour_revenue", precision = 12, scale = 2)
    private BigDecimal peakHourRevenue;

    @Column(name = "hourly_data_json", columnDefinition = "TEXT")
    private String hourlyDataJson;

    public enum PeriodType {
        DAILY,
        WEEKLY,
        MONTHLY,
        QUARTERLY,
        YEARLY
    }
}
