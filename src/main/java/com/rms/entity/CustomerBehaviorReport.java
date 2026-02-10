package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@jakarta.persistence.Table(name = "customer_behavior_reports", indexes = {
        @Index(name = "idx_customer_behavior_restaurant_date", columnList = "restaurant_id, report_date"),
        @Index(name = "idx_customer_behavior_customer", columnList = "customer_id, report_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerBehaviorReport extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false, insertable=false, updatable=false)
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private User customer;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 30)
    private SalesReport.PeriodType periodType;

    // Order Metrics
    @Column(name = "total_orders", nullable = false)
    @Builder.Default
    private Integer totalOrders = 0;

    @Column(name = "total_spent", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalSpent = BigDecimal.ZERO;

    @Column(name = "average_order_value", precision = 10, scale = 2)
    private BigDecimal averageOrderValue;

    @Column(name = "largest_order_value", precision = 10, scale = 2)
    private BigDecimal largestOrderValue;

    // Frequency Metrics
    @Column(name = "visit_frequency_days")
    private Integer visitFrequencyDays;

    @Column(name = "days_since_last_order")
    private Integer daysSinceLastOrder;

    @Column(name = "first_order_date")
    private LocalDate firstOrderDate;

    @Column(name = "last_order_date")
    private LocalDate lastOrderDate;

    // Customer Lifetime Value
    @Column(name = "lifetime_value", precision = 12, scale = 2)
    private BigDecimal lifetimeValue;

    @Column(name = "predicted_ltv", precision = 12, scale = 2)
    private BigDecimal predictedLtv;

    // Segmentation
    @Enumerated(EnumType.STRING)
    @Column(name = "customer_segment", length = 30)
    private CustomerSegment customerSegment;

    @Enumerated(EnumType.STRING)
    @Column(name = "rfm_segment", length = 30)
    private RFMSegment rfmSegment;

    // Preferences
    @Column(name = "favorite_category", length = 100)
    private String favoriteCategory;

    @Column(name = "favorite_item", length = 200)
    private String favoriteItem;

    @Column(name = "preferred_order_type", length = 30)
    private String preferredOrderType;

    @Column(name = "preferred_time_slot", length = 50)
    private String preferredTimeSlot;

    // Engagement
    @Column(name = "discount_usage_count", nullable = false)
    @Builder.Default
    private Integer discountUsageCount = 0;

    @Column(name = "loyalty_points_earned", nullable = false)
    @Builder.Default
    private Integer loyaltyPointsEarned = 0;

    @Column(name = "reviews_written", nullable = false)
    @Builder.Default
    private Integer reviewsWritten = 0;

    @Column(name = "average_rating_given", precision = 3, scale = 2)
    private BigDecimal averageRatingGiven;

    // Churn Risk
    @Column(name = "churn_risk_score", precision = 5, scale = 2)
    private BigDecimal churnRiskScore;

    @Column(name = "is_at_risk", nullable = false)
    @Builder.Default
    private Boolean isAtRisk = false;

    public enum CustomerSegment {
        NEW,
        REGULAR,
        VIP,
        AT_RISK,
        LOST,
        CHAMPION
    }

    public enum RFMSegment {
        CHAMPION,           // High recency, frequency, monetary
        LOYAL_CUSTOMER,     // High frequency
        POTENTIAL_LOYALIST,
        NEW_CUSTOMER,
        PROMISING,
        NEED_ATTENTION,
        ABOUT_TO_SLEEP,
        AT_RISK,
        CANT_LOSE,
        HIBERNATING,
        LOST
    }
}
