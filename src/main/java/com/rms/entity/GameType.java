package com.rms.entity;

// GameType.java
//package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@jakarta.persistence.Table(name = "game_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameType extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "game_mode", nullable = false, length = 30)
    private GameMode gameMode;

    @Column(name = "min_players", nullable = false)
    @Builder.Default
    private Integer minPlayers = 1;

    @Column(name = "max_players", nullable = false)
    @Builder.Default
    private Integer maxPlayers = 4;

    @Column(name = "average_duration_seconds", nullable = false)
    private Integer averageDurationSeconds;

    @Column(name = "base_points_per_game", nullable = false)
    @Builder.Default
    private Integer basePointsPerGame = 100;

    @Column(name = "difficulty_level")
    @Builder.Default
    private Integer difficultyLevel = 1;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "config_json", columnDefinition = "TEXT")
    private String configJson;

    public enum GameMode {
        SINGLE_PLAYER,
        MULTIPLAYER_COMPETITIVE,
        MULTIPLAYER_COOPERATIVE,
        TEAM_BASED,
        TURN_BASED,
        REAL_TIME
    }
}