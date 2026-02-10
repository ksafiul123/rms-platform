package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@jakarta.persistence.Table(name = "customer_wallets", indexes = {
        @Index(name = "idx_wallet_customer", columnList = "customer_id"),
        @Index(name = "idx_wallet_restaurant", columnList = "restaurant_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerWallet extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id",  insertable=false, updatable=false)
    private Restaurant restaurant;

    @Column(name = "balance", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "BDT";

    @Column(name = "total_credited", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalCredited = BigDecimal.ZERO;

    @Column(name = "total_debited", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalDebited = BigDecimal.ZERO;

    @Column(name = "total_refunded", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalRefunded = BigDecimal.ZERO;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_locked", nullable = false)
    @Builder.Default
    private Boolean isLocked = false;

    @Column(name = "lock_reason", length = 500)
    private String lockReason;

    @Column(name = "last_transaction_at")
    private LocalDateTime lastTransactionAt;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL)
    @Builder.Default
    private List<WalletTransaction> transactions = new ArrayList<>();
}
