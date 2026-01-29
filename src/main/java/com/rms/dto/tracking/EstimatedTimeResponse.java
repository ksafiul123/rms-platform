package com.rms.dto.tracking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstimatedTimeResponse {
    private Long orderId;
    private LocalDateTime estimatedReadyTime;
    private LocalDateTime estimatedDeliveryTime;
    private Integer estimatedMinutesRemaining;
    private String message;
}
