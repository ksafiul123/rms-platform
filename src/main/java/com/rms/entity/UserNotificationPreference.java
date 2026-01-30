package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_notification_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserNotificationPreference extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "email_enabled", nullable = false)
    @Builder.Default
    private Boolean emailEnabled = true;

    @Column(name = "email_order_updates", nullable = false)
    @Builder.Default
    private Boolean emailOrderUpdates = true;

    @Column(name = "email_promotional", nullable = false)
    @Builder.Default
    private Boolean emailPromotional = true;

    @Column(name = "email_newsletter", nullable = false)
    @Builder.Default
    private Boolean emailNewsletter = false;

    @Column(name = "whatsapp_enabled", nullable = false)
    @Builder.Default
    private Boolean whatsappEnabled = true;

    @Column(name = "whatsapp_order_updates", nullable = false)
    @Builder.Default
    private Boolean whatsappOrderUpdates = true;

    @Column(name = "whatsapp_promotional", nullable = false)
    @Builder.Default
    private Boolean whatsappPromotional = false;

    @Column(name = "push_enabled", nullable = false)
    @Builder.Default
    private Boolean pushEnabled = true;

    @Column(name = "push_order_updates", nullable = false)
    @Builder.Default
    private Boolean pushOrderUpdates = true;

    @Column(name = "push_promotional", nullable = false)
    @Builder.Default
    private Boolean pushPromotional = true;

    @Column(name = "push_deals", nullable = false)
    @Builder.Default
    private Boolean pushDeals = true;

    @Column(name = "sms_enabled", nullable = false)
    @Builder.Default
    private Boolean smsEnabled = false;

    @Column(name = "quiet_hours_enabled", nullable = false)
    @Builder.Default
    private Boolean quietHoursEnabled = false;

    @Column(name = "quiet_hours_start", length = 5)
    private String quietHoursStart;

    @Column(name = "quiet_hours_end", length = 5)
    private String quietHoursEnd;
}
