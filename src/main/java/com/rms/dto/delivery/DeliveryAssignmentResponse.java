package com.rms.dto.delivery;

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
public class DeliveryAssignmentResponse {
    private Long id;
    private Long orderId;
    private String orderNumber;
    private String deliveryPartnerName;
    private String deliveryPartnerPhone;
    private String status;

    private LocalDateTime assignedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime estimatedDeliveryTime;

    private String customerAddress;
    private String customerPhone;
    private BigDecimal currentLatitude;
    private BigDecimal currentLongitude;
    private BigDecimal distanceRemainingKm;

    private String deliveryNotes;
    private Integer totalDeliveryTimeMinutes;
}
