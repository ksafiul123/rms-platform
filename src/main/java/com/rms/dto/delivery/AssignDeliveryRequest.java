package com.rms.dto.delivery;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignDeliveryRequest {
    private Long deliveryPartnerId;
    private Integer estimatedTimeMinutes;
    private String notes;
}
