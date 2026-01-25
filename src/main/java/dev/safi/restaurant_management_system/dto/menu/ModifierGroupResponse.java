package dev.safi.restaurant_management_system.dto.menu;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Modifier Group Response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Modifier group details")
public class ModifierGroupResponse {

    private Long id;
    private String name;
    private String description;
    private String selectionType;
    private Integer minSelections;
    private Integer maxSelections;
    private Boolean isRequired;
    private Integer displayOrder;
    private List<ModifierOptionResponse> options;
}
