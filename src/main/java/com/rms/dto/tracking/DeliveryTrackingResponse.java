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

    private Double currentLatitude;
    private Double currentLongitude;
    private Double restaurantLatitude;
    private Double restaurantLongitude;
    private Double customerLatitude;
    private Double customerLongitude;

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
        private Double latitude;
        private Double longitude;
        private LocalDateTime timestamp;
    }
}
