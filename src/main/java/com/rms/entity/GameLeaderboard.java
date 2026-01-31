package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "game_leaderboards", indexes = {
        @Index(name = "idx_leaderboard_period", columnList = "period_type, period_date"),
        @Index(name = "idx_leaderboard_rank", columnList = "rank"),
        @Index(name = "idx_leaderboard_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameLeaderboard extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 20)
    private PeriodType periodType;

    @Column(name = "period_date", nullable = false)
    private LocalDate periodDate;

    @Column(name = "total_points", nullable = false)
    @Builder.Default
    private Integer totalPoints = 0;

    @Column(name = "total_games_played", nullable = false)
    @Builder.Default
    private Integer totalGamesPlayed = 0;

    @Column(name = "games_won", nullable = false)
    @Builder.Default
    private Integer gamesWon = 0;

    @Column(name = "win_rate", precision = 5, scale = 2)
    private BigDecimal winRate;

    @Column(name = "rank", nullable = false)
    @Builder.Default
    private Integer rank = 0;

    @Column(name = "average_score", precision = 10, scale = 2)
    private BigDecimal averageScore;

    @Column(name = "highest_score")
    @Builder.Default
    private Integer highestScore = 0;

    @Column(name = "total_discount_earned", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalDiscountEarned = BigDecimal.ZERO;

    public enum PeriodType {
        DAILY,
        WEEKLY,
        MONTHLY,
        ALL_TIME
    }
}
