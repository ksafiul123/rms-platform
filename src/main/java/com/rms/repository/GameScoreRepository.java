package com.rms.repository;

import com.rms.entity.GameScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameScoreRepository extends JpaRepository<GameScore, Long> {

    List<GameScore> findByGamePlayerIdOrderByScoredAtDesc(Long gamePlayerId);

    @Query("SELECT gs FROM GameScore gs " +
            "WHERE gs.gamePlayer.id = :playerId " +
            "AND gs.scoreType = :scoreType")
    List<GameScore> findByPlayerAndScoreType(
            @Param("playerId") Long playerId,
            @Param("scoreType") GameScore.ScoreType scoreType);
}
