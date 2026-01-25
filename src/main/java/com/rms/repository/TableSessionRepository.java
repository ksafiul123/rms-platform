package com.rms.repository;

import com.rms.entity.TableSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TableSessionRepository extends JpaRepository<TableSession, Long> {

    Optional<TableSession> findByIdAndRestaurantId(Long id, Long restaurantId);

    Optional<TableSession> findBySessionCode(String sessionCode);

    @Query("SELECT ts FROM TableSession ts WHERE ts.table.id = :tableId " +
            "AND ts.status = 'ACTIVE'")
    Optional<TableSession> findActiveSessionByTableId(@Param("tableId") Long tableId);

    Page<TableSession> findByRestaurantId(Long restaurantId, Pageable pageable);

    Page<TableSession> findByRestaurantIdAndStatus(
            Long restaurantId,
            TableSession.SessionStatus status,
            Pageable pageable
    );

    @Query("SELECT ts FROM TableSession ts WHERE ts.restaurantId = :restaurantId " +
            "AND ts.status = 'ACTIVE'")
    List<TableSession> findActiveSessionsByRestaurant(@Param("restaurantId") Long restaurantId);

    @Query("SELECT ts FROM TableSession ts " +
            "JOIN ts.guests g WHERE g.userId = :userId " +
            "AND ts.status = 'ACTIVE'")
    List<TableSession> findActiveSessionsByUserId(@Param("userId") Long userId);

    boolean existsBySessionCode(String sessionCode);
}
