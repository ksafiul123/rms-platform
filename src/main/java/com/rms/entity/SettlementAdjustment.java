package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "settlement_adjustments", indexes = {
        @Index(name = "idx_adjustment_settlement", columnList = "settlement_id"),
        @Index(name = "idx_adjustment_restaurant", columnList = "restaurant_id"),
        @Index(name = "idx_adjustment_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementAdjustment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id")
    private OrderSettlement settlement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "adjustment_reference", nullable = false, unique = true, length = 100)
    private String adjustmentReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "adjustment_type", nullable = false, length = 30)
    private AdjustmentType adjustmentType;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AdjustmentStatus status;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public enum AdjustmentType {
        BONUS,
        PENALTY,
        CORRECTION,
        PROMOTIONAL,
        CHARGEBACK,
        DISPUTE_RESOLUTION
    }

    public enum AdjustmentStatus {
        PENDING,
        APPROVED,
        REJECTED,
        APPLIED
    }
}
