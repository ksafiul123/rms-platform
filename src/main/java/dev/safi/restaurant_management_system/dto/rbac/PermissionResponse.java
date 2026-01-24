package dev.safi.restaurant_management_system.dto.rbac;

//package com.rms.dto.rbac;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Permission Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Permission details")
public class PermissionResponse {

    @Schema(description = "Permission ID", example = "1")
    private Long id;

    @Schema(description = "Permission name", example = "menu:create")
    private String name;

    @Schema(description = "Display name", example = "Create Menu")
    private String displayName;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Resource", example = "menu")
    private String resource;

    @Schema(description = "Action type", example = "CREATE")
    private String action;

    @Schema(description = "Category", example = "MENU_MANAGEMENT")
    private String category;

    @Schema(description = "Is system permission")
    private Boolean isSystem;

    @Schema(description = "Is active")
    private Boolean isActive;
}

