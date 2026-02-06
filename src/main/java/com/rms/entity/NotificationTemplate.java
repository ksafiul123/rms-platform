package com.rms.entity;

// NotificationTemplate.java
//package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@jakarta.persistence.Table(name = "notification_templates", indexes = {
        @Index(name = "idx_template_code", columnList = "code"),
        @Index(name = "idx_template_channel", columnList = "channel")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationTemplate extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private NotificationType type;

    @Column(name = "subject", length = 200)
    private String subject;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "html_content", columnDefinition = "TEXT")
    private String htmlContent;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "action_url", length = 500)
    private String actionUrl;

    @Column(name = "action_text", length = 100)
    private String actionText;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "priority", nullable = false)
    @Builder.Default
    private Integer priority = 0;

    public enum NotificationChannel {
        EMAIL,
        WHATSAPP,
        PUSH,
        SMS
    }

    public enum NotificationType {
        TRANSACTIONAL,
        PROMOTIONAL,
        ALERT,
        INFORMATIONAL
    }
}