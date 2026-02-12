package com.rms.repository;

import com.rms.entity.GamePlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GamePlayerRepository extends JpaRepository<GamePlayer, Long> {

    @Query("SELECT gp FROM GamePlayer gp WHERE gp.gameSession.id = :gameSessionId")
    List<GamePlayer> findByGameSessionId(@Param("gameSessionId") Long gameSessionId);

    @Query("SELECT gp FROM GamePlayer gp WHERE gp.user.id = :userId ORDER BY gp.joinedAt DESC")
    List<GamePlayer> findByUserIdOrderByJoinedAtDesc(@Param("userId") Long userId);

    @Query("SELECT gp FROM GamePlayer gp " +
            "WHERE gp.gameSession.id = :sessionId " +
            "AND gp.isActive = true")
    List<GamePlayer> findActivePlayersBySession(@Param("sessionId") Long sessionId);

    @Query("SELECT COUNT(gp) FROM GamePlayer gp " +
            "WHERE gp.user.id = :userId " +
            "AND gp.rank = 1")
    Long countWinsByUser(@Param("userId") Long userId);
}
