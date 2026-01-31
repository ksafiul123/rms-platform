package com.rms.repository;

import com.rms.entity.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GameSessionRepository extends JpaRepository<GameSession, Long> {

    Optional<GameSession> findBySessionCode(String sessionCode);

    Optional<GameSession> findByTableSessionId(Long tableSessionId);

    List<GameSession> findByStatus(GameSession.SessionStatus status);

    @Query("SELECT gs FROM GameSession gs " +
            "WHERE gs.restaurant.id = :restaurantId " +
            "AND gs.startedAt >= :since " +
            "ORDER BY gs.startedAt DESC")
    List<GameSession> findRecentSessions(
            @Param("restaurantId") Long restaurantId,
            @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(gs) FROM GameSession gs " +
            "WHERE gs.restaurant.id = :restaurantId " +
            "AND gs.status = 'COMPLETED' " +
            "AND gs.startedAt >= :since")
    Long countCompletedSessions(
            @Param("restaurantId") Long restaurantId,
            @Param("since") LocalDateTime since);
}
