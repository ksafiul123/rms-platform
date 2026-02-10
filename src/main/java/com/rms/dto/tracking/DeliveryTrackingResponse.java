package com.rms.dto.tracking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryTrackingResponse {
    private Long orderId;
    private String orderNumber;
    private String deliveryPartnerName;
    private String deliveryPartnerPhone;
    private String vehicleNumber;

    private BigDecimal currentLatitude;
    private BigDecimal currentLongitude;
    private BigDecimal restaurantLatitude;
    private BigDecimal restaurantLongitude;
    private BigDecimal customerLatitude;
    private BigDecimal customerLongitude;

    private BigDecimal distanceRemainingKm;
    private Integer estimatedMinutesRemaining;
    private LocalDateTime lastLocationUpdate;

    private String deliveryStatus;
    private List<DeliveryTrackingResponse.LocationHistory> locationHistory;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationHistory {
        private BigDecimal latitude;
        private BigDecimal longitude;
        private LocalDateTime timestamp;
    }
}
