package com.rms.dto.kitchen;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarkReadyRequest {
    private String notes;
    private Boolean notifyCustomer = true;
}
