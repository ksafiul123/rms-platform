package com.rms.dto.kitchen;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KitchenOrderItemResponse {
    private Long id;
    private Long orderItemId;
    private String itemName;
    private Integer quantity;
    private String status;

    private String assignedChefName;
    private String station;
    private Integer priority;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer preparationTimeMinutes;

    private List<String> modifiers;
    private String specialInstructions;
    private String preparationNotes;
}
