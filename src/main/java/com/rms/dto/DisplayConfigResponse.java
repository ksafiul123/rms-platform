package com.rms.dto;

import com.rms.entity.DisplayConfiguration;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class DisplayConfigResponse {
    private Long restaurantId;
    private String restaurantName;
    private DisplayConfiguration.DisplayMode displayMode;
    private DisplayConfiguration.Theme theme;
    private Integer refreshIntervalSeconds;

    private Boolean showPreparing;
    private Boolean showReady;
    private Boolean showCompleted;
    private Integer maxOrdersDisplay;
    private Boolean showOrderItems;
    private Boolean showEstimatedTime;
    private Boolean showElapsedTime;

    private StyleConfig style;
    private TextConfig text;

    @Data
    @Builder
    public static class StyleConfig {
        private String primaryColor;
        private String secondaryColor;
        private String readyColor;
        private String preparingColor;
        private String fontFamily;
        private String logoUrl;
        private String backgroundImageUrl;
    }

    @Data
    @Builder
    public static class TextConfig {
        private String headerText;
        private String footerText;
        private String language;
        private Map<String, String> translations;
    }
}
