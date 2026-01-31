package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "commission_rules", indexes = {
        @Index(name = "idx_commission_restaurant", columnList = "restaurant_id"),
        @Index(name = "idx_commission_active", columnList = "is_active"),
        @Index(name = "idx_commission_dates", columnList = "effective_from, effective_to")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionRule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;

    @Enumerated(EnumType.STRING)
    @Column(name = "commission_type", nullable = false, length = 30)
    private CommissionType commissionType;

    @Column(name = "commission_percentage", precision = 5, scale = 2)
    private BigDecimal commissionPercentage;

    @Column(name = "fixed_fee_per_order", precision = 10, scale = 2)
    private BigDecimal fixedFeePerOrder;

    @Column(name = "min_commission_amount", precision = 10, scale = 2)
    private BigDecimal minCommissionAmount;

    @Column(name = "max_commission_amount", precision = 10, scale = 2)
    private BigDecimal maxCommissionAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "applies_to_order_type", length = 30)
    private Order.OrderType appliesToOrderType;

    @Column(name = "min_order_amount", precision = 10, scale = 2)
    private BigDecimal minOrderAmount;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "priority", nullable = false)
    @Builder.Default
    private Integer priority = 0;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "commissionRule", cascade = CascadeType.ALL)
    @Builder.Default
    private List<CommissionTier> tiers = new ArrayList<>();

    public enum CommissionType {
        PERCENTAGE,
        FIXED,
        TIERED,
        HYBRID
    }
}
