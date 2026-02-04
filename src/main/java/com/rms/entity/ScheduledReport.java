package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "scheduled_reports", indexes = {
        @Index(name = "idx_scheduled_restaurant", columnList = "restaurant_id"),
        @Index(name = "idx_scheduled_active", columnList = "is_active, next_run_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledReport extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "report_name", nullable = false, length = 200)
    private String reportName;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 50)
    private AnalyticsSnapshot.ReportType reportType;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false, length = 30)
    private Frequency frequency;

    @Column(name = "schedule_time", length = 5)
    private String scheduleTime;  // HH:mm format

    @Column(name = "schedule_day_of_week")
    private Integer scheduleDayOfWeek;  // 1-7 for weekly

    @Column(name = "schedule_day_of_month")
    private Integer scheduleDayOfMonth;  // 1-31 for monthly

    @Enumerated(EnumType.STRING)
    @Column(name = "export_format", nullable = false, length = 20)
    private ExportFormat exportFormat;

    @Column(name = "recipients", columnDefinition = "TEXT")
    private String recipients;  // JSON array of email addresses

    @Column(name = "filters_json", columnDefinition = "TEXT")
    private String filtersJson;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "last_run_at")
    private LocalDateTime lastRunAt;

    @Column(name = "next_run_at")
    private LocalDateTime nextRunAt;

    @Column(name = "execution_count", nullable = false)
    @Builder.Default
    private Integer executionCount = 0;

    @Column(name = "last_status", length = 30)
    private String lastStatus;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    public enum Frequency {
        DAILY,
        WEEKLY,
        MONTHLY,
        QUARTERLY,
        CUSTOM
    }

    public enum ExportFormat {
        PDF,
        EXCEL,
        CSV,
        JSON
    }
}
