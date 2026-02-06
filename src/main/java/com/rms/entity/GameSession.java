package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@jakarta.persistence.Table(name = "game_sessions", indexes = {
        @Index(name = "idx_game_session_table", columnList = "table_session_id"),
        @Index(name = "idx_game_session_status", columnList = "status"),
        @Index(name = "idx_game_session_started", columnList = "started_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_session_id", nullable = false)
    private TableSession tableSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_type_id", nullable = false)
    private GameType gameType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "session_code", nullable = false, unique = true, length = 20)
    private String sessionCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SessionStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "total_rounds", nullable = false)
    @Builder.Default
    private Integer totalRounds = 1;

    @Column(name = "current_round", nullable = false)
    @Builder.Default
    private Integer currentRound = 1;

    @Column(name = "player_count", nullable = false)
    @Builder.Default
    private Integer playerCount = 0;

    @Column(name = "winner_id")
    private Long winnerId;

    @Column(name = "total_points_awarded")
    @Builder.Default
    private Integer totalPointsAwarded = 0;

    @Column(name = "discount_earned", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountEarned = BigDecimal.ZERO;

    @Column(name = "game_data", columnDefinition = "TEXT")
    private String gameData;

    @Column(name = "completed_successfully", nullable = false)
    @Builder.Default
    private Boolean completedSuccessfully = false;

    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL)
    @Builder.Default
    private List<GamePlayer> players = new ArrayList<>();

    public enum SessionStatus {
        WAITING,
        IN_PROGRESS,
        PAUSED,
        COMPLETED,
        ABANDONED,
        EXPIRED
    }
}
