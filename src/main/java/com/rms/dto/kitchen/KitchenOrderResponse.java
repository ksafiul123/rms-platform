package com.rms.dto.kitchen;

// KitchenOrderResponse.java
//package com.rms.dto.kitchen;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KitchenOrderResponse {
    private Long orderId;
    private String orderNumber;
    private String orderType;
    private String tableNumber;
    private String customerName;
    private String status;
    private Integer priority;
    private Boolean isRushOrder;

    private LocalDateTime orderTime;
    private LocalDateTime estimatedReadyTime;
    private Integer estimatedPreparationTimeMinutes;
    private Integer elapsedTimeMinutes;

    private List<KitchenOrderItemResponse> items;
    private String kitchenNotes;
    private String specialInstructions;

    private BigDecimal totalAmount;
    private Integer totalItems;
    private Integer itemsPrepared;
}

