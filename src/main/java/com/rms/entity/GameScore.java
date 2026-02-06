package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@jakarta.persistence.Table(name = "game_scores", indexes = {
        @Index(name = "idx_game_score_player", columnList = "game_player_id"),
        @Index(name = "idx_game_score_timestamp", columnList = "scored_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameScore extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_player_id", nullable = false)
    private GamePlayer gamePlayer;

    @Enumerated(EnumType.STRING)
    @Column(name = "score_type", nullable = false, length = 30)
    private ScoreType scoreType;

    @Column(name = "points", nullable = false)
    private Integer points;

    @Column(name = "round_number")
    private Integer roundNumber;

    @Column(name = "scored_at", nullable = false)
    private LocalDateTime scoredAt;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    public enum ScoreType {
        CORRECT_ANSWER,
        WRONG_ANSWER,
        TIME_BONUS,
        ACCURACY_BONUS,
        STREAK_BONUS,
        COMBO_MULTIPLIER,
        COMPLETION_BONUS,
        PERFECT_ROUND,
        SPEED_BONUS,
        TEAM_CONTRIBUTION
    }
}
