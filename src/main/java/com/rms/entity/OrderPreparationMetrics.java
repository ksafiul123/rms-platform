package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_preparation_metrics", indexes = {
        @Index(name = "idx_metrics_order", columnList = "order_id"),
        @Index(name = "idx_metrics_performance", columnList = "was_on_time, delay_minutes")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPreparationMetrics extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    // Timestamps
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "kitchen_started_at")
    private LocalDateTime kitchenStartedAt;

    @Column(name = "kitchen_completed_at")
    private LocalDateTime kitchenCompletedAt;

    @Column(name = "ready_at")
    private LocalDateTime readyAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    // Duration calculations (in minutes)
    @Column(name = "time_to_confirm")
    private Integer timeToConfirm;

    @Column(name = "time_to_start_preparing")
    private Integer timeToStartPreparing;

    @Column(name = "actual_preparation_time")
    private Integer actualPreparationTime;

    @Column(name = "time_to_ready")
    private Integer timeToReady;

    @Column(name = "total_time_to_delivery")
    private Integer totalTimeToDelivery;

    // Target vs Actual
    @Column(name = "target_preparation_time")
    private Integer targetPreparationTime;

    @Column(name = "was_on_time", nullable = false)
    @Builder.Default
    private Boolean wasOnTime = true;

    @Column(name = "delay_minutes")
    private Integer delayMinutes;

    // Kitchen performance
    @Column(name = "total_items")
    private Integer totalItems;

    @Column(name = "items_completed_on_time")
    private Integer itemsCompletedOnTime;

    @Column(name = "complexity_score")
    private Integer complexityScore;

    public void calculateMetrics() {
        if (confirmedAt != null && readyAt != null) {
            timeToReady = (int) java.time.Duration.between(confirmedAt, readyAt).toMinutes();
        }

        if (confirmedAt != null && kitchenStartedAt != null) {
            timeToStartPreparing = (int) java.time.Duration.between(confirmedAt, kitchenStartedAt).toMinutes();
        }

        if (kitchenStartedAt != null && kitchenCompletedAt != null) {
            actualPreparationTime = (int) java.time.Duration.between(kitchenStartedAt, kitchenCompletedAt).toMinutes();
        }

        if (targetPreparationTime != null && actualPreparationTime != null) {
            if (actualPreparationTime > targetPreparationTime) {
                wasOnTime = false;
                delayMinutes = actualPreparationTime - targetPreparationTime;
            }
        }
    }
}
