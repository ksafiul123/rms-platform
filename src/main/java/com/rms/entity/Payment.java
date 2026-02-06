package com.rms.entity;

// Payment.java
//package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@jakarta.persistence.Table(name = "payments", indexes = {
        @Index(name = "idx_payment_order", columnList = "order_id"),
        @Index(name = "idx_payment_reference", columnList = "payment_reference"),
        @Index(name = "idx_payment_status", columnList = "status"),
        @Index(name = "idx_payment_customer", columnList = "customer_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "payment_reference", nullable = false, unique = true, length = 100)
    private String paymentReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_provider", length = 30)
    private PaymentProvider paymentProvider;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "BDT";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "provider_transaction_id", length = 200)
    private String providerTransactionId;

    @Column(name = "provider_payment_intent_id", length = 200)
    private String providerPaymentIntentId;

    @Column(name = "provider_response", columnDefinition = "TEXT")
    private String providerResponse;

    @Column(name = "initiated_at", nullable = false)
    private LocalDateTime initiatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal refundAmount = BigDecimal.ZERO;

    @Column(name = "is_refunded", nullable = false)
    @Builder.Default
    private Boolean isRefunded = false;

    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    @Column(name = "customer_email", length = 100)
    private String customerEmail;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL)
    @Builder.Default
    private List<PaymentTransaction> transactions = new ArrayList<>();

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL)
    @Builder.Default
    private List<PaymentRefund> refunds = new ArrayList<>();

    @OneToMany(mappedBy = "parentPayment", cascade = CascadeType.ALL)
    @Builder.Default
    private List<PaymentSplitDetail> splitDetails = new ArrayList<>();

    public enum PaymentMethod {
        ONLINE_CARD,
        BKASH,
        NAGAD,
        ROCKET,
        CASH,
        CARD_TERMINAL,
        WALLET,
        SPLIT
    }

    public enum PaymentProvider {
        STRIPE,
        SSL_COMMERZ,
        BKASH,
        NAGAD,
        ROCKET,
        RAZORPAY,
        INTERNAL,
        MANUAL
    }

    public enum PaymentStatus {
        PENDING,
        PROCESSING,
        AUTHORIZED,
        COMPLETED,
        FAILED,
        CANCELLED,
        REFUND_PENDING,
        REFUNDED,
        PARTIALLY_REFUNDED,
        EXPIRED
    }
}

