package com.rms.dto.restaurant;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Restaurant Settings Response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Restaurant settings")
public class RestaurantSettingsResponse {

    private String businessName;
    private String businessType;
    private String currency;
    private String timezone;
    private String language;
    private BigDecimal taxPercentage;
    private BigDecimal serviceChargePercentage;
    private Boolean autoAcceptOrders;
    private Boolean allowOnlinePayments;
    private Boolean allowCashPayments;
    private BigDecimal minimumOrderAmount;
    private BigDecimal deliveryRadiusKm;
    private Integer averagePreparationTimeMinutes;
    private String logoUrl;
    private String bannerUrl;
}
