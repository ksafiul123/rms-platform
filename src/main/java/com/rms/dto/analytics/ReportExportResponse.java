package com.rms.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportExportResponse {
    private String reportId;
    private String fileName;
    private String downloadUrl;
    private Long fileSize;
    private String format;
    private LocalDateTime generatedAt;
    private LocalDateTime expiresAt;
}
