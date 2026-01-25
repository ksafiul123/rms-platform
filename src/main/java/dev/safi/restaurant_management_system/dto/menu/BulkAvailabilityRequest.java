package dev.safi.restaurant_management_system.dto.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Bulk Availability Update Request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Bulk availability update")
public class BulkAvailabilityRequest {

    @NotNull(message = "Item IDs are required")
    @Size(min = 1)
    @Schema(description = "Menu item IDs")
    private List<Long> itemIds;

    @NotNull(message = "Availability status is required")
    @Schema(description = "Is available", example = "true")
    private Boolean isAvailable;

    @Schema(description = "Reason for change")
    private String reason;
}
