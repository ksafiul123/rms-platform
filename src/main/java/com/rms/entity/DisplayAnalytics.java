package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@jakarta.persistence.Table(name = "display_analytics", indexes = {
        @Index(name = "idx_analytics_restaurant", columnList = "restaurant_id"),
        @Index(name = "idx_analytics_date", columnList = "analytics_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisplayAnalytics extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "analytics_date", nullable = false)
    private LocalDate analyticsDate;

    @Column(name = "total_orders_displayed", nullable = false)
    @Builder.Default
    private Integer totalOrdersDisplayed = 0;

    @Column(name = "avg_display_duration_minutes")
    private Integer avgDisplayDurationMinutes;

    @Column(name = "avg_time_to_ready_minutes")
    private Integer avgTimeToReadyMinutes;

    @Column(name = "avg_time_to_collect_minutes")
    private Integer avgTimeToCollectMinutes;

    @Column(name = "peak_concurrent_orders")
    private Integer peakConcurrentOrders;

    @Column(name = "total_display_views", nullable = false)
    @Builder.Default
    private Long totalDisplayViews = 0L;

    @Column(name = "unique_display_sessions")
    private Integer uniqueDisplaySessions;

    @Column(name = "avg_refresh_rate_seconds")
    private Integer avgRefreshRateSeconds;
}
