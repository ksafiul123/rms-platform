package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@jakarta.persistence.Table(name = "game_players", indexes = {
        @Index(name = "idx_game_player_session", columnList = "game_session_id"),
        @Index(name = "idx_game_player_user", columnList = "user_id"),
        @Index(name = "idx_game_player_score", columnList = "final_score")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GamePlayer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id", nullable = false)
    private GameSession gameSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "player_name", nullable = false, length = 100)
    private String playerName;

    @Column(name = "guest_identifier", length = 100)
    private String guestIdentifier;

    @Column(name = "team_id")
    private Integer teamId;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "current_score", nullable = false)
    @Builder.Default
    private Integer currentScore = 0;

    @Column(name = "final_score")
    private Integer finalScore;

    @Column(name = "rank")
    private Integer rank;

    @Column(name = "accuracy_percentage", precision = 5, scale = 2)
    private BigDecimal accuracyPercentage;

    @Column(name = "completion_time_seconds")
    private Integer completionTimeSeconds;

    @Column(name = "correct_answers")
    @Builder.Default
    private Integer correctAnswers = 0;

    @Column(name = "wrong_answers")
    @Builder.Default
    private Integer wrongAnswers = 0;

    @Column(name = "bonus_points")
    @Builder.Default
    private Integer bonusPoints = 0;

    @Column(name = "combo_multiplier", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal comboMultiplier = BigDecimal.ONE;

    @Column(name = "achievements_earned", columnDefinition = "TEXT")
    private String achievementsEarned;

    @OneToMany(mappedBy = "gamePlayer", cascade = CascadeType.ALL)
    @Builder.Default
    private List<GameScore> scores = new ArrayList<>();
}
