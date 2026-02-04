package com.rms.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportExportRequest {
    private String reportType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String format; // CSV, EXCEL, PDF
    private List<String> includeFields;
    private Map<String, String> filters;
}
