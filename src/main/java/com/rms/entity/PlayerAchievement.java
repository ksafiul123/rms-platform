package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "player_achievements", indexes = {
        @Index(name = "idx_achievement_user", columnList = "user_id"),
        @Index(name = "idx_achievement_type", columnList = "achievement_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerAchievement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "achievement_type", nullable = false, length = 30)
    private AchievementType achievementType;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "points_awarded", nullable = false)
    private Integer pointsAwarded;

    @Column(name = "earned_at", nullable = false)
    private LocalDateTime earnedAt;

    @Column(name = "progress_value")
    private Integer progressValue;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Column(name = "badge_tier", length = 20)
    private String badgeTier;

    public enum AchievementType {
        FIRST_GAME,
        FIRST_WIN,
        PERFECT_SCORE,
        SPEED_DEMON,
        ACCURACY_MASTER,
        STREAK_CHAMPION,
        SOCIAL_BUTTERFLY,
        GAME_VETERAN,
        POINT_MILESTONE,
        DISCOUNT_HUNTER,
        DAILY_PLAYER,
        GAME_MASTER
    }
}
