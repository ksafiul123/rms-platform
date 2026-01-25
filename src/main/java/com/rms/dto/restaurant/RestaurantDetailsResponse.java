package com.rms.dto.restaurant;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Restaurant Details Response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Complete restaurant details")
public class RestaurantDetailsResponse {

    @Schema(description = "Restaurant ID")
    private Long id;

    @Schema(description = "Restaurant code")
    private String restaurantCode;

    @Schema(description = "Restaurant name")
    private String name;

    @Schema(description = "Email")
    private String email;

    @Schema(description = "Phone number")
    private String phoneNumber;

    @Schema(description = "Address")
    private String address;

    @Schema(description = "Settings")
    private RestaurantSettingsResponse settings;

    @Schema(description = "Subscription details")
    private SubscriptionResponse subscription;

    @Schema(description = "Enabled features")
    private List<String> enabledFeatures;

    @Schema(description = "Active status")
    private Boolean isActive;

    @Schema(description = "Created at")
    private LocalDateTime createdAt;
}
