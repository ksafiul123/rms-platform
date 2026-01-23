package dev.safi.restaurant_management_system.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantInfo {
    private Long restaurantId;
    private String restaurantCode;
    private String restaurantName;
    private String adminEmail;
    private String subscriptionStatus;
    private LocalDateTime subscriptionExpiry;
}