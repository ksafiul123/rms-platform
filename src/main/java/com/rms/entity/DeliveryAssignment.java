package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@jakarta.persistence.Table(name = "delivery_assignments", indexes = {
        @Index(name = "idx_delivery_order", columnList = "order_id"),
        @Index(name = "idx_delivery_partner", columnList = "delivery_partner_id"),
        @Index(name = "idx_delivery_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryAssignment extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_partner_id", nullable = false)
    private User deliveryPartner;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private DeliveryStatus status = DeliveryStatus.ASSIGNED;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "picked_up_at")
    private LocalDateTime pickedUpAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "estimated_pickup_time")
    private LocalDateTime estimatedPickupTime;

    @Column(name = "estimated_delivery_time")
    private LocalDateTime estimatedDeliveryTime;

    @Column(name = "current_latitude", precision = 10, scale = 8)
    private Double currentLatitude;

    @Column(name = "current_longitude", precision = 11, scale = 8)
    private Double currentLongitude;

    @Column(name = "last_location_update")
    private LocalDateTime lastLocationUpdate;

    @Column(name = "distance_remaining_km", precision = 8, scale = 2)
    private BigDecimal distanceRemainingKm;

    @Column(name = "delivery_notes", columnDefinition = "TEXT")
    private String deliveryNotes;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "customer_feedback", columnDefinition = "TEXT")
    private String customerFeedback;

    public enum DeliveryStatus {
        ASSIGNED,
        ACCEPTED,
        REJECTED,
        PICKED_UP,
        IN_TRANSIT,
        ARRIVED,
        DELIVERED,
        FAILED
    }

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (assignedAt == null) {
            assignedAt = LocalDateTime.now();
        }
    }

    public Integer getTotalDeliveryTimeMinutes() {
        if (assignedAt != null && deliveredAt != null) {
            return (int) java.time.Duration.between(assignedAt, deliveredAt).toMinutes();
        }
        return null;
    }
}
