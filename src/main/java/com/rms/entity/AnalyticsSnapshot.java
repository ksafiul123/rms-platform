package com.rms.entity;

// Analytics & Reporting System - Entity Classes
// Part of Restaurant Management System
// 8 entities for comprehensive analytics and reporting

//package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

// ============================================
// 1. AnalyticsSnapshot
// Pre-calculated analytics for fast reporting
// ============================================

@Entity
@jakarta.persistence.Table(name = "analytics_snapshots", indexes = {
        @Index(name = "idx_snapshot_restaurant_date", columnList = "restaurant_id, snapshot_date, report_type"),
        @Index(name = "idx_snapshot_date", columnList = "snapshot_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsSnapshot extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false, insertable=false, updatable=false)
    private Restaurant restaurant;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 50)
    private ReportType reportType;

    @Enumerated(EnumType.STRING)
    @Column(name = "time_period", nullable = false, length = 30)
    private TimePeriod timePeriod;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "data_json", columnDefinition = "TEXT", nullable = false)
    private String dataJson;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Column(name = "is_cached", nullable = false)
    @Builder.Default
    private Boolean isCached = true;

    @Column(name = "cache_expires_at")
    private LocalDateTime cacheExpiresAt;

    public enum ReportType {
        SALES_SUMMARY,
        MENU_PERFORMANCE,
        CUSTOMER_BEHAVIOR,
        INVENTORY_USAGE,
        OPERATIONAL_METRICS,
        FINANCIAL_SUMMARY,
        REVENUE_FORECAST,
        CUSTOM_REPORT
    }

    public enum TimePeriod {
        DAILY,
        WEEKLY,
        MONTHLY,
        QUARTERLY,
        YEARLY,
        CUSTOM
    }
}

