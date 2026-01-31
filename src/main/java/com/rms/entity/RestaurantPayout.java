package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "restaurant_payouts", indexes = {
        @Index(name = "idx_payout_restaurant", columnList = "restaurant_id"),
        @Index(name = "idx_payout_status", columnList = "payout_status"),
        @Index(name = "idx_payout_date", columnList = "payout_date"),
        @Index(name = "idx_payout_reference", columnList = "payout_reference")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantPayout extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "payout_reference", nullable = false, unique = true, length = 100)
    private String payoutReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "payout_type", nullable = false, length = 30)
    private PayoutType payoutType;

    @Column(name = "period_start_date", nullable = false)
    private LocalDate periodStartDate;

    @Column(name = "period_end_date", nullable = false)
    private LocalDate periodEndDate;

    @Column(name = "total_orders", nullable = false)
    private Integer totalOrders;

    @Column(name = "total_order_amount", precision = 10, scale = 2)
    private BigDecimal totalOrderAmount;

    @Column(name = "total_commission", precision = 10, scale = 2)
    private BigDecimal totalCommission;

    @Column(name = "total_fees", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalFees = BigDecimal.ZERO;

    @Column(name = "total_refunds", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalRefunds = BigDecimal.ZERO;

    @Column(name = "total_adjustments", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalAdjustments = BigDecimal.ZERO;

    @Column(name = "payout_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal payoutAmount;

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "BDT";

    @Enumerated(EnumType.STRING)
    @Column(name = "payout_method", nullable = false, length = 30)
    private PayoutMethod payoutMethod;

    @Column(name = "bank_account_number", length = 50)
    private String bankAccountNumber;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "bank_branch", length = 100)
    private String bankBranch;

    @Column(name = "account_holder_name", length = 200)
    private String accountHolderName;

    @Column(name = "mobile_money_number", length = 20)
    private String mobileMoneyNumber;

    @Column(name = "mobile_money_provider", length = 30)
    private String mobileMoneyProvider;

    @Enumerated(EnumType.STRING)
    @Column(name = "payout_status", nullable = false, length = 30)
    private PayoutStatus payoutStatus;

    @Column(name = "payout_date")
    private LocalDate payoutDate;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiated_by")
    private User initiatedBy;

    @Column(name = "initiated_at", nullable = false)
    private LocalDateTime initiatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    private User processedBy;

    @Column(name = "transaction_reference", length = 200)
    private String transactionReference;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "payout", cascade = CascadeType.ALL)
    @Builder.Default
    private List<OrderSettlement> settlements = new ArrayList<>();

    @OneToMany(mappedBy = "payout", cascade = CascadeType.ALL)
    @Builder.Default
    private List<PayoutDocument> documents = new ArrayList<>();

    public enum PayoutType {
        MANUAL,
        SCHEDULED,
        ON_DEMAND,
        EARLY_PAYOUT
    }

    public enum PayoutMethod {
        BANK_TRANSFER,
        BKASH,
        NAGAD,
        ROCKET,
        CASH,
        CHEQUE,
        WALLET_CREDIT
    }

    public enum PayoutStatus {
        DRAFT,
        PENDING_APPROVAL,
        APPROVED,
        PROCESSING,
        COMPLETED,
        FAILED,
        CANCELLED,
        REJECTED,
        ON_HOLD
    }
}
