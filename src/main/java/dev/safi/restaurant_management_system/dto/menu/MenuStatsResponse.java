package dev.safi.restaurant_management_system.dto.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Menu Stats Response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Menu statistics")
public class MenuStatsResponse {

    private Long totalCategories;
    private Long totalItems;
    private Long activeItems;
    private Long inactiveItems;
    private Long unavailableItems;
    private Long lowStockItems;
    private Long featuredItems;
    private Long bestSellerItems;
    private BigDecimal averagePrice;
    private BigDecimal lowestPrice;
    private BigDecimal highestPrice;
}
