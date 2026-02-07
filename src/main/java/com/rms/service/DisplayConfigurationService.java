package com.rms.service;

import com.rms.dto.DisplayConfigRequest;
import com.rms.dto.DisplayDataResponse;
import com.rms.dto.DisplayTokenResponse;
import com.rms.dto.DisplayUrlResponse;
import com.rms.entity.DisplayConfiguration;
import com.rms.entity.Restaurant;
import com.rms.exception.ResourceNotFoundException;
import com.rms.repository.DisplayConfigurationRepository;
import com.rms.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DisplayConfigurationService {

    private static final String DISPLAY_URL_PREFIX = "/display/";

    private final DisplayConfigurationRepository displayConfigurationRepository;
    private final RestaurantRepository restaurantRepository;
    private final DisplayService displayService;

    public DisplayConfiguration getOrCreateConfig(Long restaurantId) {
        return displayConfigurationRepository.findByRestaurantId(restaurantId)
                .orElseGet(() -> createDefaultConfig(restaurantId));
    }

    public DisplayConfiguration updateConfig(Long restaurantId, DisplayConfigRequest request) {
        DisplayConfiguration config = getOrCreateConfig(restaurantId);

        if (request.getDisplayMode() != null) {
            config.setDisplayMode(request.getDisplayMode());
        }
        if (request.getTheme() != null) {
            config.setTheme(request.getTheme());
        }
        if (request.getRefreshIntervalSeconds() != null) {
            config.setRefreshIntervalSeconds(request.getRefreshIntervalSeconds());
        }
        if (request.getShowPreparing() != null) {
            config.setShowPreparing(request.getShowPreparing());
        }
        if (request.getShowReady() != null) {
            config.setShowReady(request.getShowReady());
        }
        if (request.getShowCompleted() != null) {
            config.setShowCompleted(request.getShowCompleted());
        }
        if (request.getMaxOrdersDisplay() != null) {
            config.setMaxOrdersDisplay(request.getMaxOrdersDisplay());
        }
        if (request.getShowOrderItems() != null) {
            config.setShowOrderItems(request.getShowOrderItems());
        }
        if (request.getShowEstimatedTime() != null) {
            config.setShowEstimatedTime(request.getShowEstimatedTime());
        }
        if (request.getShowElapsedTime() != null) {
            config.setShowElapsedTime(request.getShowElapsedTime());
        }
        if (request.getPlaySoundOnReady() != null) {
            config.setPlaySoundOnReady(request.getPlaySoundOnReady());
        }
        if (request.getSoundNotificationUrl() != null) {
            config.setSoundNotificationUrl(request.getSoundNotificationUrl());
        }
        if (request.getHighlightReadyDurationSeconds() != null) {
            config.setHighlightReadyDurationSeconds(request.getHighlightReadyDurationSeconds());
        }
        if (request.getLogoUrl() != null) {
            config.setLogoUrl(request.getLogoUrl());
        }
        if (request.getBackgroundImageUrl() != null) {
            config.setBackgroundImageUrl(request.getBackgroundImageUrl());
        }
        if (request.getPrimaryColor() != null) {
            config.setPrimaryColor(request.getPrimaryColor());
        }
        if (request.getSecondaryColor() != null) {
            config.setSecondaryColor(request.getSecondaryColor());
        }
        if (request.getReadyColor() != null) {
            config.setReadyColor(request.getReadyColor());
        }
        if (request.getPreparingColor() != null) {
            config.setPreparingColor(request.getPreparingColor());
        }
        if (request.getFontFamily() != null) {
            config.setFontFamily(request.getFontFamily());
        }
        if (request.getHeaderText() != null) {
            config.setHeaderText(request.getHeaderText());
        }
        if (request.getFooterText() != null) {
            config.setFooterText(request.getFooterText());
        }
        if (request.getLanguage() != null) {
            config.setLanguage(request.getLanguage());
        }
        if (request.getIsActive() != null) {
            config.setIsActive(request.getIsActive());
        }

        return displayConfigurationRepository.save(config);
    }

    public DisplayTokenResponse regenerateToken(Long restaurantId) {
        DisplayConfiguration config = getOrCreateConfig(restaurantId);
        config.setDisplayToken(UUID.randomUUID().toString());
        displayConfigurationRepository.save(config);

        return DisplayTokenResponse.builder()
                .displayToken(config.getDisplayToken())
                .regeneratedAt(LocalDateTime.now())
                .build();
    }

    public DisplayUrlResponse getDisplayUrl(Long restaurantId) {
        DisplayConfiguration config = getOrCreateConfig(restaurantId);
        return DisplayUrlResponse.builder()
                .displayToken(config.getDisplayToken())
                .displayUrl(DISPLAY_URL_PREFIX + config.getDisplayToken())
                .build();
    }

    public DisplayDataResponse getPreviewData(Long restaurantId) {
        DisplayConfiguration config = getOrCreateConfig(restaurantId);
        return displayService.getLiveOrderData(config.getDisplayToken(), "preview");
    }

    private DisplayConfiguration createDefaultConfig(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        DisplayConfiguration config = DisplayConfiguration.builder()
                .restaurant(restaurant)
                .displayToken(UUID.randomUUID().toString())
                .build();

        return displayConfigurationRepository.save(config);
    }
}
