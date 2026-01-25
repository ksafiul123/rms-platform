package com.rms.repository;

import com.rms.entity.TableSessionGuest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TableSessionGuestRepository extends JpaRepository<TableSessionGuest, Long> {

    Optional<TableSessionGuest> findBySessionIdAndUserId(Long sessionId, Long userId);

    List<TableSessionGuest> findBySessionIdAndStatus(
            Long sessionId,
            TableSessionGuest.GuestStatus status
    );

    @Query("SELECT COUNT(g) FROM TableSessionGuest g WHERE g.session.id = :sessionId " +
            "AND g.status = 'ACTIVE'")
    Long countActiveGuestsBySessionId(@Param("sessionId") Long sessionId);

    boolean existsBySessionIdAndUserId(Long sessionId, Long userId);
}
