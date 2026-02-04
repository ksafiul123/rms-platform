package com.rms.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchComparisonResponse {
    private String metricName;
    private List<BranchMetric> branches;
    private Long topPerformingBranchId;
    private Long lowestPerformingBranchId;
}
