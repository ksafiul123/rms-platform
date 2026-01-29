package com.rms.dto.delivery;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationUpdateRequest {
    private Double latitude;
    private Double longitude;
    private Double distanceRemainingKm;
}
