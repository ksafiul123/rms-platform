package com.rms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Display Stats Response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisplayStatsResponse {

    private Integer totalOrders;
    private Integer readyCount;
    private Integer preparingCount;
    private Integer completedCount;
    private Integer avgPreparationTime;
    private Integer peakWaitTime;
}
