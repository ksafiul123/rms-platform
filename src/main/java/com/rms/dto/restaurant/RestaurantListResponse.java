package com.rms.dto.restaurant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Restaurant List Response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Restaurant list item")
public class RestaurantListResponse {

    private Long id;
    private String restaurantCode;
    private String name;
    private String email;
    private String phoneNumber;
    private String subscriptionStatus;
    private LocalDateTime subscriptionExpiry;
    private Integer daysRemaining;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
