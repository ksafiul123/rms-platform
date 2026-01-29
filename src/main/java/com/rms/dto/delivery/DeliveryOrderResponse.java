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
public class DeliveryOrderResponse {
    private Long orderId;
    private String orderNumber;
    private String customerName;
    private String customerPhone;
    private String deliveryAddress;
    private BigDecimal totalAmount;
    private LocalDateTime readyTime;
    private String status;
    private Integer priority;

    private Double restaurantLatitude;
    private Double restaurantLongitude;
    private Double customerLatitude;
    private Double customerLongitude;
    private Double distanceKm;
}
