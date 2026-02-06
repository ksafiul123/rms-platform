package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@jakarta.persistence.Table(name = "payment_webhooks", indexes = {
        @Index(name = "idx_webhook_provider", columnList = "provider"),
        @Index(name = "idx_webhook_received", columnList = "received_at"),
        @Index(name = "idx_webhook_event_id", columnList = "event_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentWebhook extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 30)
    private Payment.PaymentProvider provider;

    @Column(name = "event_id", nullable = false, unique = true, length = 200)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "payload", columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Column(name = "signature", length = 500)
    private String signature;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "is_processed", nullable = false)
    @Builder.Default
    private Boolean isProcessed = false;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "processing_error", columnDefinition = "TEXT")
    private String processingError;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;
}
