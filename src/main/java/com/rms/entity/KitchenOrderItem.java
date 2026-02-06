package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@jakarta.persistence.Table(name = "kitchen_order_items", indexes = {
        @Index(name = "idx_kitchen_items_order", columnList = "order_id"),
        @Index(name = "idx_kitchen_items_status", columnList = "status"),
        @Index(name = "idx_kitchen_items_assigned", columnList = "assigned_to")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KitchenOrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedChef;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private ItemStatus status = ItemStatus.NOT_STARTED;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "preparation_notes", columnDefinition = "TEXT")
    private String preparationNotes;

    @Column(name = "priority", nullable = false)
    @Builder.Default
    private Integer priority = 0;

    @Column(name = "station", length = 100)
    private String station;

    public enum ItemStatus {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED,
        ON_HOLD,
        CANCELLED
    }

    public Integer getPreparationTimeMinutes() {
        if (startedAt != null && completedAt != null) {
            return (int) java.time.Duration.between(startedAt, completedAt).toMinutes();
        }
        return null;
    }
}
