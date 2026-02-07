package com.rms.dto;

import com.rms.entity.DisplayConfiguration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisplayConfigRequest {

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
    private Boolean playSoundOnReady;
    private String soundNotificationUrl;
    private Integer highlightReadyDurationSeconds;
    private String logoUrl;
    private String backgroundImageUrl;
    private String primaryColor;
    private String secondaryColor;
    private String readyColor;
    private String preparingColor;
    private String fontFamily;
    private String headerText;
    private String footerText;
    private String language;
    private Boolean isActive;
}
