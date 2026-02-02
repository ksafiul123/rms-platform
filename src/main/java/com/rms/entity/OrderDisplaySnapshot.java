package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_display_snapshots", indexes = {
        @Index(name = "idx_snapshot_restaurant", columnList = "restaurant_id"),
        @Index(name = "idx_snapshot_status", columnList = "display_status"),
        @Index(name = "idx_snapshot_updated", columnList = "last_updated")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDisplaySnapshot extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(name = "order_number", nullable = false, length = 50)
    private String orderNumber;

    @Column(name = "display_number", length = 20)
    private String displayNumber;

    @Column(name = "table_number", length = 20)
    private String tableNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 30)
    private Order.OrderType orderType;

    @Enumerated(EnumType.STRING)
    @Column(name = "display_status", nullable = false, length = 30)
    private DisplayStatus displayStatus;

    @Column(name = "customer_name", length = 100)
    private String customerName;

    @Column(name = "total_items", nullable = false)
    private Integer totalItems;

    @Column(name = "items_completed", nullable = false)
    @Builder.Default
    private Integer itemsCompleted = 0;

    @Column(name = "estimated_ready_time")
    private LocalDateTime estimatedReadyTime;

    @Column(name = "actual_ready_time")
    private LocalDateTime actualReadyTime;

    @Column(name = "elapsed_minutes")
    private Integer elapsedMinutes;

    @Column(name = "remaining_minutes")
    private Integer remainingMinutes;

    @Column(name = "priority", nullable = false)
    @Builder.Default
    private Integer priority = 0;

    @Column(name = "is_highlighted", nullable = false)
    @Builder.Default
    private Boolean isHighlighted = false;

    @Column(name = "highlighted_at")
    private LocalDateTime highlightedAt;

    @Column(name = "display_position")
    private Integer displayPosition;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @Column(name = "items_json", columnDefinition = "TEXT")
    private String itemsJson;

    public enum DisplayStatus {
        PREPARING,
        READY,
        CALLED,
        COLLECTED,
        HIDDEN
    }
}
