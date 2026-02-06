package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@jakarta.persistence.Table(name = "settlement_reconciliations", indexes = {
        @Index(name = "idx_reconciliation_restaurant", columnList = "restaurant_id"),
        @Index(name = "idx_reconciliation_period", columnList = "period_start_date, period_end_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementReconciliation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "reconciliation_reference", nullable = false, unique = true, length = 100)
    private String reconciliationReference;

    @Column(name = "period_start_date", nullable = false)
    private LocalDate periodStartDate;

    @Column(name = "period_end_date", nullable = false)
    private LocalDate periodEndDate;

    @Column(name = "total_settlements", nullable = false)
    private Integer totalSettlements;

    @Column(name = "total_settlement_amount", precision = 10, scale = 2)
    private BigDecimal totalSettlementAmount;

    @Column(name = "total_payouts", nullable = false)
    private Integer totalPayouts;

    @Column(name = "total_payout_amount", precision = 10, scale = 2)
    private BigDecimal totalPayoutAmount;

    @Column(name = "variance_amount", precision = 10, scale = 2)
    private BigDecimal varianceAmount;

    @Column(name = "discrepancies_found", nullable = false)
    private Integer discrepanciesFound;

    @Enumerated(EnumType.STRING)
    @Column(name = "reconciliation_status", nullable = false, length = 30)
    private ReconciliationStatus reconciliationStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by", nullable = false)
    private User performedBy;

    @Column(name = "performed_at", nullable = false)
    private LocalDateTime performedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public enum ReconciliationStatus {
        IN_PROGRESS,
        RECONCILED,
        DISCREPANCY_FOUND,
        RESOLVED
    }
}
