package com.rms.dto.delivery;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliverOrderRequest {
    private String notes;
    private String customerSignature;
    private String proofOfDeliveryImage;
}
