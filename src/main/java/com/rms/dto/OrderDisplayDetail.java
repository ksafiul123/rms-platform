package com.rms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Order Display Detail Response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDisplayDetail {

    private String orderNumber;
    private String displayNumber;
    private String tableNumber;
    private String orderType;
    private String status;
    private String customerName;
    private Integer totalItems;
    private LocalDateTime estimatedReadyTime;
    private LocalDateTime actualReadyTime;
    private Integer elapsedMinutes;
    private Integer remainingMinutes;
    private List<OrderItemDetail> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemDetail {
        private String itemName;
        private Integer quantity;
        private String specialInstructions;
    }
}
