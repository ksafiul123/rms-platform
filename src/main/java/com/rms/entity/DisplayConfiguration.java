package com.rms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.*;
import lombok.NoArgsConstructor;

@Entity
@jakarta.persistence.Table(name = "display_configurations", indexes = {
        @Index(name = "idx_display_restaurant", columnList = "restaurant_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisplayConfiguration extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false, unique = true, insertable=false, updatable=false)
    private Restaurant restaurant;

    @Enumerated(EnumType.STRING)
    @Column(name = "display_mode", nullable = false, length = 30)
    @Builder.Default
    private DisplayMode displayMode = DisplayMode.ORDER_NUMBER;

    @Enumerated(EnumType.STRING)
    @Column(name = "theme", nullable = false, length = 30)
    @Builder.Default
    private Theme theme = Theme.LIGHT;

    @Column(name = "refresh_interval_seconds", nullable = false)
    @Builder.Default
    private Integer refreshIntervalSeconds = 5;

    @Column(name = "show_preparing", nullable = false)
    @Builder.Default
    private Boolean showPreparing = true;

    @Column(name = "show_ready", nullable = false)
    @Builder.Default
    private Boolean showReady = true;

    @Column(name = "show_completed", nullable = false)
    @Builder.Default
    private Boolean showCompleted = false;

    @Column(name = "max_orders_display", nullable = false)
    @Builder.Default
    private Integer maxOrdersDisplay = 20;

    @Column(name = "show_order_items", nullable = false)
    @Builder.Default
    private Boolean showOrderItems = false;

    @Column(name = "show_estimated_time", nullable = false)
    @Builder.Default
    private Boolean showEstimatedTime = true;

    @Column(name = "show_elapsed_time", nullable = false)
    @Builder.Default
    private Boolean showElapsedTime = false;

    @Column(name = "play_sound_on_ready", nullable = false)
    @Builder.Default
    private Boolean playSoundOnReady = true;

    @Column(name = "sound_notification_url", length = 500)
    private String soundNotificationUrl;

    @Column(name = "highlight_ready_duration_seconds")
    @Builder.Default
    private Integer highlightReadyDurationSeconds = 30;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "background_image_url", length = 500)
    private String backgroundImageUrl;

    @Column(name = "primary_color", length = 7)
    @Builder.Default
    private String primaryColor = "#007bff";

    @Column(name = "secondary_color", length = 7)
    @Builder.Default
    private String secondaryColor = "#6c757d";

    @Column(name = "ready_color", length = 7)
    @Builder.Default
    private String readyColor = "#28a745";

    @Column(name = "preparing_color", length = 7)
    @Builder.Default
    private String preparingColor = "#ffc107";

    @Column(name = "font_family", length = 100)
    @Builder.Default
    private String fontFamily = "Arial, sans-serif";

    @Column(name = "header_text", length = 200)
    private String headerText;

    @Column(name = "footer_text", length = 200)
    private String footerText;

    @Column(name = "language", length = 5)
    @Builder.Default
    private String language = "en";

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "display_token", nullable = false, unique = true, length = 100)
    private String displayToken;

    public enum DisplayMode {
        ORDER_NUMBER,      // Show order numbers only
        TABLE_NUMBER,      // Show table numbers
        DETAILED_ITEMS,    // Show order items
        TIMELINE,          // Show progress timeline
        GRID_LAYOUT,       // Grid of order cards
        CAROUSEL           // Rotating carousel
    }

    public enum Theme {
        LIGHT,
        DARK,
        HIGH_CONTRAST,
        CUSTOM
    }
}
