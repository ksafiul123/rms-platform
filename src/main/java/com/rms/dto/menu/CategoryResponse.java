package com.rms.dto.menu;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Category Response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Menu category details")
public class CategoryResponse {

    private Long id;
    private Long restaurantId;
    private String name;
    private String description;
    private String imageUrl;
    private String iconName;
    private Integer displayOrder;
    private Long parentCategoryId;
    private String parentCategoryName;
    private List<CategoryResponse> subCategories;
    private Integer itemCount;
    private Boolean isActive;
    private Boolean isFeatured;
    private LocalTime availableFrom;
    private LocalTime availableTo;
    private LocalDateTime createdAt;
}
