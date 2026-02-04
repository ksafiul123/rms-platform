package com.rms.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSegmentationResponse {
    private Integer totalCustomers;
    private Map<CustomerSegment, List<CustomerSegmentData>> segments;
    private Integer vipCount;
    private Integer regularCount;
    private Integer occasionalCount;
    private Integer atRiskCount;
    private Integer lostCount;
}
