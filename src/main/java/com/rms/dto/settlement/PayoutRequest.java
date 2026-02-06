package com.rms.dto.settlement;

import com.rms.entity.RestaurantPayout;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PayoutRequest {

    private LocalDate periodStart;
    private LocalDate periodEnd;
    private RestaurantPayout.PayoutMethod payoutMethod;
    private RestaurantPayout.PayoutType payoutType;
}
