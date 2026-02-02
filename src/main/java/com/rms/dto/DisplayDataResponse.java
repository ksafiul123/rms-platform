package com.rms.dto;

import com.rms.entity.DisplayConfiguration;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DisplayDataResponse {

    private String restaurantName;
    private String headerText;
    private String footerText;
    private DisplayConfiguration.DisplayMode displayMode;
    private DisplayConfiguration.Theme theme;
    private LocalDateTime currentTime;
    private Long serverTimestamp;

    private List<OrderDisplayData> readyOrders;
    private List<OrderDisplayData> preparingOrders;
    private List<OrderDisplayData> completedOrders;

    private DisplayStats stats;
    private StyleConfig styleConfig;

    @Data
    @Builder
    public static class OrderDisplayData {
        private String orderNumber;
        private String displayNumber;
        private String tableNumber;
        private String orderType;
        private String status;
        private Integer priority;
        private Boolean isHighlighted;

        private String customerName;
        private Integer totalItems;
        private Integer itemsCompleted;
        private Integer progressPercentage;

        private LocalDateTime estimatedReadyTime;
        private LocalDateTime actualReadyTime;
        private Integer elapsedMinutes;
        private Integer remainingMinutes;
        private String timeDisplay;

        private List<OrderItemDisplay> items;

        @Data
        @Builder
        public static class OrderItemDisplay {
            private String itemName;
            private Integer quantity;
            private Boolean isCompleted;
            private String status;
        }
    }

    @Data
    @Builder
    public static class DisplayStats {
        private Integer totalOrders;
        private Integer readyCount;
        private Integer preparingCount;
        private Integer completedCount;
        private Integer avgPreparationTime;
        private Integer peakWaitTime;
    }

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
        private Boolean playSoundOnReady;
        private String soundNotificationUrl;
    }
}
