package com.rms.repository;

import com.rms.entity.PermissionAuditLog;
import com.rms.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Permission Audit Log Repository
 */
@Repository
public interface PermissionAuditLogRepository extends JpaRepository<PermissionAuditLog, Long> {

    Page<PermissionAuditLog> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);

    Page<PermissionAuditLog> findByRestaurantIdOrderByTimestampDesc(Long restaurantId, Pageable pageable);

    Page<PermissionAuditLog> findByUserIdAndRestaurantIdOrderByTimestampDesc(
            Long userId, Long restaurantId, Pageable pageable
    );

    @Query("SELECT pal FROM PermissionAuditLog pal " +
            "WHERE pal.userId = :userId " +
            "AND pal.timestamp BETWEEN :startDate AND :endDate " +
            "ORDER BY pal.timestamp DESC")
    List<PermissionAuditLog> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT pal FROM PermissionAuditLog pal " +
            "WHERE pal.accessGranted = false " +
            "AND pal.timestamp > :since " +
            "ORDER BY pal.timestamp DESC")
    List<PermissionAuditLog> findRecentAccessDenials(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(pal) FROM PermissionAuditLog pal " +
            "WHERE pal.userId = :userId " +
            "AND pal.action = :action " +
            "AND pal.timestamp > :since")
    Long countUserActionsSince(
            @Param("userId") Long userId,
            @Param("action") AuditAction action,
            @Param("since") LocalDateTime since
    );

    @Query("SELECT pal.resource, COUNT(pal) as accessCount " +
            "FROM PermissionAuditLog pal " +
            "WHERE pal.restaurantId = :restaurantId " +
            "AND pal.accessGranted = true " +
            "AND pal.timestamp > :since " +
            "GROUP BY pal.resource " +
            "ORDER BY accessCount DESC")
    List<Object[]> findMostAccessedResources(
            @Param("restaurantId") Long restaurantId,
            @Param("since") LocalDateTime since,
            Pageable pageable
    );
}
