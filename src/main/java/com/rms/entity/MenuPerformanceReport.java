package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "menu_performance_reports", indexes = {
        @Index(name = "idx_menu_perf_restaurant_date", columnList = "restaurant_id, report_date"),
        @Index(name = "idx_menu_perf_item", columnList = "menu_item_id, report_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuPerformanceReport extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private MenuCategory category;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 30)
    private SalesReport.PeriodType periodType;

    // Sales Metrics
    @Column(name = "quantity_sold", nullable = false)
    @Builder.Default
    private Integer quantitySold = 0;

    @Column(name = "total_revenue", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(name = "total_cost", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalCost = BigDecimal.ZERO;

    @Column(name = "gross_profit", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal grossProfit = BigDecimal.ZERO;

    @Column(name = "profit_margin", precision = 5, scale = 2)
    private BigDecimal profitMargin;

    // Performance Indicators
    @Column(name = "popularity_rank")
    private Integer popularityRank;

    @Column(name = "revenue_rank")
    private Integer revenueRank;

    @Column(name = "profit_rank")
    private Integer profitRank;

    @Column(name = "average_price", precision = 10, scale = 2)
    private BigDecimal averagePrice;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    // Customer Behavior
    @Column(name = "unique_customers", nullable = false)
    @Builder.Default
    private Integer uniqueCustomers = 0;

    @Column(name = "repeat_customer_percentage", precision = 5, scale = 2)
    private BigDecimal repeatCustomerPercentage;

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Column(name = "total_reviews", nullable = false)
    @Builder.Default
    private Integer totalReviews = 0;

    // Operational Metrics
    @Column(name = "average_prep_time_minutes")
    private Integer averagePrepTimeMinutes;

    @Column(name = "stock_out_incidents", nullable = false)
    @Builder.Default
    private Integer stockOutIncidents = 0;

    @Column(name = "refund_count", nullable = false)
    @Builder.Default
    private Integer refundCount = 0;

    // Time Analysis
    @Column(name = "peak_time_sales")
    private String peakTimeSales;

    @Column(name = "slow_time_sales")
    private String slowTimeSales;
}
