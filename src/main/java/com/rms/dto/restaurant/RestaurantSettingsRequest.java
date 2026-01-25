package com.rms.dto.restaurant;

//package com.rms.dto.restaurant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Restaurant Settings Update Request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Restaurant settings update request")
public class RestaurantSettingsRequest {

    @Schema(description = "Business name", example = "Golden Fork Restaurant")
    private String businessName;

    @Schema(description = "Tax registration number", example = "TAX123456")
    private String taxRegistrationNumber;

    @Schema(description = "GST number", example = "GST987654")
    private String gstNumber;

    @Schema(description = "Business type", example = "CASUAL_DINING")
    private String businessType;

    @Schema(description = "Currency code", example = "BDT")
    private String currency;

    @Schema(description = "Timezone", example = "Asia/Dhaka")
    private String timezone;

    @Schema(description = "Language", example = "en")
    private String language;

    @Schema(description = "Tax percentage", example = "5.00")
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    private BigDecimal taxPercentage;

    @Schema(description = "Service charge percentage", example = "10.00")
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    private BigDecimal serviceChargePercentage;

    @Schema(description = "Auto accept orders", example = "false")
    private Boolean autoAcceptOrders;

    @Schema(description = "Allow online payments", example = "true")
    private Boolean allowOnlinePayments;

    @Schema(description = "Allow cash payments", example = "true")
    private Boolean allowCashPayments;

    @Schema(description = "Minimum order amount", example = "100.00")
    @DecimalMin(value = "0.0")
    private BigDecimal minimumOrderAmount;

    @Schema(description = "Delivery radius in km", example = "10.00")
    @DecimalMin(value = "0.0")
    private BigDecimal deliveryRadiusKm;

    @Schema(description = "Average preparation time in minutes", example = "30")
    @Min(1)
    @Max(300)
    private Integer averagePreparationTimeMinutes;

    @Schema(description = "Logo URL")
    private String logoUrl;

    @Schema(description = "Banner URL")
    private String bannerUrl;
}

