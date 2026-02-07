package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@jakarta.persistence.Table(name = "discount_rewards", indexes = {
        @Index(name = "idx_discount_user", columnList = "user_id"),
        @Index(name = "idx_discount_order", columnList = "order_id"),
        @Index(name = "idx_discount_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscountReward extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id", nullable = false)
    private GameSession gameSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "reward_code", nullable = false, unique = true, length = 20)
    private String rewardCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false, length = 30)
    private RewardType rewardType;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "max_discount_amount", precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "min_order_amount", precision = 10, scale = 2)
    private BigDecimal minOrderAmount;

    @Column(name = "points_earned", nullable = false)
    private Integer pointsEarned;

    @Column(name = "earned_at", nullable = false)
    private LocalDateTime earnedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RewardStatus status;

    @Column(name = "description", length = 500)
    private String description;

    public enum RewardType {
        PERCENTAGE_DISCOUNT,
        FIXED_AMOUNT,
        FREE_ITEM,
        UPGRADE,
        BONUS_POINTS,
        NEXT_ORDER_DISCOUNT
    }

    public enum RewardStatus {
        ACTIVE,
        APPLIED,
        EXPIRED,
        CANCELLED
    }
}
