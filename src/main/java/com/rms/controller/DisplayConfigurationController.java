package com.rms.controller;

import com.rms.dto.DisplayConfigRequest;
import com.rms.dto.DisplayDataResponse;
import com.rms.dto.DisplayTokenResponse;
import com.rms.dto.DisplayUrlResponse;
import com.rms.dto.auth.ApiResponse;
import com.rms.entity.DisplayConfiguration;
import com.rms.service.DisplayConfigurationService;
import com.rms.security.annotation.RequirePermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/display")
@RequiredArgsConstructor
public class DisplayConfigurationController {

    private final DisplayConfigurationService displayConfigService;

    // Get display configuration
    @GetMapping("/restaurant/{restaurantId}/config")
    @RequirePermission("display:view")
    public ResponseEntity<ApiResponse<DisplayConfiguration>> getConfig(
            @PathVariable Long restaurantId) {

        DisplayConfiguration config = displayConfigService.getOrCreateConfig(restaurantId);
        return ResponseEntity.ok(ApiResponse.success(config));
    }

    // Update display configuration
    @PutMapping("/restaurant/{restaurantId}/config")
    @RequirePermission("display:manage")
    public ResponseEntity<ApiResponse<DisplayConfiguration>> updateConfig(
            @PathVariable Long restaurantId,
            @RequestBody @Valid DisplayConfigRequest request) {

        DisplayConfiguration config = displayConfigService.updateConfig(restaurantId, request);
        return ResponseEntity.ok(ApiResponse.success("Display configuration updated", config));
    }

    // Generate new display token
    @PostMapping("/restaurant/{restaurantId}/regenerate-token")
    @RequirePermission("display:manage")
    public ResponseEntity<ApiResponse<DisplayTokenResponse>> regenerateToken(
            @PathVariable Long restaurantId) {

        DisplayTokenResponse response = displayConfigService.regenerateToken(restaurantId);
        return ResponseEntity.ok(ApiResponse.success("Display token regenerated", response));
    }

    // Get display URL
    @GetMapping("/restaurant/{restaurantId}/url")
    @RequirePermission("display:view")
    public ResponseEntity<ApiResponse<DisplayUrlResponse>> getDisplayUrl(
            @PathVariable Long restaurantId) {

        DisplayUrlResponse response = displayConfigService.getDisplayUrl(restaurantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Preview display
    @GetMapping("/restaurant/{restaurantId}/preview")
    @RequirePermission("display:view")
    public ResponseEntity<ApiResponse<DisplayDataResponse>> previewDisplay(
            @PathVariable Long restaurantId) {

        DisplayDataResponse preview = displayConfigService.getPreviewData(restaurantId);
        return ResponseEntity.ok(ApiResponse.success(preview));
    }
}
