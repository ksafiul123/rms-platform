package dev.safi.restaurant_management_system.dto.menu;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Modifier Option Response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Modifier option details")
public class ModifierOptionResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal priceAdjustment;
    private Boolean isDefault;
    private Boolean isAvailable;
    private Integer displayOrder;
}
