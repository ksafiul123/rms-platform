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

    @Query("SELECT tsg FROM TableSessionGuest tsg WHERE tsg.session.id = :sessionId AND tsg.userId = :userId")
    Optional<TableSessionGuest> findBySessionIdAndUserId(@Param("sessionId") Long sessionId, @Param("userId") Long userId);

    @Query("SELECT tsg FROM TableSessionGuest tsg WHERE tsg.session.id = :sessionId AND tsg.status = :status")
    List<TableSessionGuest> findBySessionIdAndStatus(
            @Param("sessionId") Long sessionId,
            @Param("status") TableSessionGuest.GuestStatus status
    );

    @Query("SELECT COUNT(g) FROM TableSessionGuest g WHERE g.session.id = :sessionId " +
            "AND g.status = 'ACTIVE'")
    Long countActiveGuestsBySessionId(@Param("sessionId") Long sessionId);

    @Query("SELECT CASE WHEN COUNT(tsg) > 0 THEN true ELSE false END FROM TableSessionGuest tsg WHERE tsg.session.id = :sessionId AND tsg.userId = :userId")
    boolean existsBySessionIdAndUserId(@Param("sessionId") Long sessionId, @Param("userId") Long userId);
}
