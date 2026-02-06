package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@jakarta.persistence.Table(name = "payment_refunds", indexes = {
        @Index(name = "idx_refund_payment", columnList = "payment_id"),
        @Index(name = "idx_refund_status", columnList = "status"),
        @Index(name = "idx_refund_reference", columnList = "refund_reference")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRefund extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "refund_reference", nullable = false, unique = true, length = 100)
    private String refundReference;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_type", nullable = false, length = 30)
    private RefundType refundType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RefundStatus status;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiated_by")
    private User initiatedBy;

    @Column(name = "initiated_at", nullable = false)
    private LocalDateTime initiatedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "provider_refund_id", length = 200)
    private String providerRefundId;

    @Column(name = "provider_response", columnDefinition = "TEXT")
    private String providerResponse;

    @Column(name = "refund_method", length = 30)
    private String refundMethod;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public enum RefundType {
        FULL,
        PARTIAL,
        CANCELLATION,
        DISPUTE,
        GOODWILL
    }

    public enum RefundStatus {
        PENDING,
        APPROVED,
        PROCESSING,
        COMPLETED,
        FAILED,
        REJECTED,
        CANCELLED
    }
}
