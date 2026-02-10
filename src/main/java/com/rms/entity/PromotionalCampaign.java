package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@jakarta.persistence.Table(name = "promotional_campaigns", indexes = {
        @Index(name = "idx_campaign_restaurant", columnList = "restaurant_id"),
        @Index(name = "idx_campaign_status", columnList = "status"),
        @Index(name = "idx_campaign_scheduled", columnList = "scheduled_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionalCampaign extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false, insertable=false, updatable=false)
    private Restaurant restaurant;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "campaign_type", nullable = false, length = 30)
    private CampaignType campaignType;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_audience", nullable = false, length = 30)
    private TargetAudience targetAudience;

    @Column(name = "channels", length = 100)
    private String channels;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private NotificationTemplate template;

    @Column(name = "discount_code", length = 50)
    private String discountCode;

    @Column(name = "discount_percentage")
    private Integer discountPercentage;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CampaignStatus status;

    @Column(name = "total_sent")
    @Builder.Default
    private Integer totalSent = 0;

    @Column(name = "total_delivered")
    @Builder.Default
    private Integer totalDelivered = 0;

    @Column(name = "total_read")
    @Builder.Default
    private Integer totalRead = 0;

    @Column(name = "total_conversions")
    @Builder.Default
    private Integer totalConversions = 0;

    public enum CampaignType {
        FLASH_DEAL,
        SEASONAL_OFFER,
        NEW_MENU_LAUNCH,
        CUSTOMER_RETENTION,
        CART_ABANDONMENT,
        BIRTHDAY_SPECIAL,
        LOYALTY_REWARD
    }

    public enum TargetAudience {
        ALL_CUSTOMERS,
        NEW_CUSTOMERS,
        REGULAR_CUSTOMERS,
        VIP_CUSTOMERS,
        INACTIVE_CUSTOMERS,
        CUSTOM_SEGMENT
    }

    public enum CampaignStatus {
        DRAFT,
        SCHEDULED,
        RUNNING,
        COMPLETED,
        PAUSED,
        CANCELLED
    }
}
