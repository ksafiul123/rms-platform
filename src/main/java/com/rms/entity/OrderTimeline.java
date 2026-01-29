package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_timeline", indexes = {
        @Index(name = "idx_timeline_order", columnList = "order_id"),
        @Index(name = "idx_timeline_timestamp", columnList = "timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderTimeline extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private EventType eventType;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "icon", length = 50)
    private String icon;

    @Column(name = "is_milestone", nullable = false)
    @Builder.Default
    private Boolean isMilestone = false;

    public enum EventType {
        ORDER_PLACED,
        ORDER_CONFIRMED,
        PAYMENT_RECEIVED,
        KITCHEN_STARTED,
        FOOD_PREPARING,
        FOOD_READY,
        DELIVERY_ASSIGNED,
        DELIVERY_PICKED_UP,
        OUT_FOR_DELIVERY,
        DELIVERED,
        CANCELLED,
        ISSUE_REPORTED,
        REFUND_PROCESSED
    }

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
