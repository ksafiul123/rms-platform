package com.rms.dto.kitchen;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StartPreparationRequest {
    private Integer estimatedTimeMinutes;
    private String notes;
    private List<ItemAssignment> itemAssignments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemAssignment {
        private Long orderItemId;
        private Long assignedChefId;
        private String station;
    }
}
