package com.rms.dto.tracking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveOrderStatusResponse {
    private Long orderId;
    private String orderNumber;
    private String currentStatus;
    private String currentStatusDisplay;
    private Integer progressPercentage;

    private LocalDateTime orderTime;
    private LocalDateTime estimatedReadyTime;
    private LocalDateTime actualReadyTime;
    private LocalDateTime estimatedDeliveryTime;

    private Integer remainingMinutes;
    private String statusMessage;
    private String nextStatus;

    private Integer totalItems;
    private Integer itemsPrepared;
    private Integer itemsRemaining;

    private String deliveryPartnerName;
    private String deliveryPartnerPhone;
    private String deliveryStatus;
    private BigDecimal deliveryLatitude;
    private BigDecimal deliveryLongitude;
    private BigDecimal distanceRemainingKm;

    private Boolean canCancel;
    private Boolean canTrackDelivery;
}
