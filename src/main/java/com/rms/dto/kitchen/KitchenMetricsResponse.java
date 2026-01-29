package com.rms.dto.kitchen;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KitchenMetricsResponse {
    private LocalDate date;
    private Integer totalOrders;
    private Integer completedOrders;
    private Integer inProgressOrders;
    private Double averagePreparationTime;
    private Integer ordersOnTime;
    private Integer ordersDelayed;
    private Double onTimePercentage;
    private Integer totalItems;
    private Integer itemsCompletedOnTime;
}
