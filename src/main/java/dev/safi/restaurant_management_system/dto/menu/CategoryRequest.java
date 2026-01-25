package dev.safi.restaurant_management_system.dto.menu;

//package com.rms.dto.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * Category Create/Update Request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Menu category request")
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100)
    @Schema(description = "Category name", example = "Appetizers")
    private String name;

    @Size(max = 500)
    @Schema(description = "Category description")
    private String description;

    @Schema(description = "Parent category ID for nested categories")
    private Long parentCategoryId;

    @Schema(description = "Display order", example = "1")
    private Integer displayOrder;

    @Schema(description = "Category image URL")
    private String imageUrl;

    @Schema(description = "Icon name", example = "utensils")
    private String iconName;

    @Schema(description = "Available from time", example = "09:00")
    private LocalTime availableFrom;

    @Schema(description = "Available to time", example = "22:00")
    private LocalTime availableTo;

    @Schema(description = "Is featured category", example = "false")
    private Boolean isFeatured;
}

