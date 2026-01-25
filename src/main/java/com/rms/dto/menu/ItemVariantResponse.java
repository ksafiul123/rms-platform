package com.rms.dto.menu;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Item Variant Response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Item variant details")
public class ItemVariantResponse {

    private Long id;
    private String name;
    private String sku;
    private BigDecimal priceAdjustment;
    private BigDecimal finalPrice;
    private Boolean isDefault;
    private Boolean isAvailable;
    private Integer displayOrder;
}
