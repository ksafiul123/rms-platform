package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "report_exports", indexes = {
        @Index(name = "idx_export_restaurant", columnList = "restaurant_id"),
        @Index(name = "idx_export_user", columnList = "user_id"),
        @Index(name = "idx_export_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportExport extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheduled_report_id")
    private ScheduledReport scheduledReport;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 50)
    private AnalyticsSnapshot.ReportType reportType;

    @Column(name = "report_name", nullable = false, length = 200)
    private String reportName;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "export_format", nullable = false, length = 20)
    private ScheduledReport.ExportFormat exportFormat;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "download_url", length = 500)
    private String downloadUrl;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ExportStatus status;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "download_count", nullable = false)
    @Builder.Default
    private Integer downloadCount = 0;

    @Column(name = "last_downloaded_at")
    private LocalDateTime lastDownloadedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    public enum ExportStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        EXPIRED
    }
}
