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
public class OrderTimelineResponse {
    private Long id;
    private String eventType;
    private String title;
    private String description;
    private LocalDateTime timestamp;
    private String icon;
    private Boolean isMilestone;
    private String displayTime;
}
