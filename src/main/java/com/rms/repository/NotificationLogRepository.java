package com.rms.repository;

import com.rms.entity.NotificationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    Page<NotificationLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<NotificationLog> findByStatusAndRetryCountLessThan(
            NotificationLog.NotificationStatus status, Integer maxRetries);

    @Query("SELECT n FROM NotificationLog n " +
            "WHERE n.restaurant.id = :restaurantId " +
            "AND n.sentAt BETWEEN :start AND :end " +
            "ORDER BY n.sentAt DESC")
    List<NotificationLog> findByRestaurantAndDateRange(
            @Param("restaurantId") Long restaurantId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT n.channel, n.status, COUNT(n) " +
            "FROM NotificationLog n " +
            "WHERE n.sentAt >= :since " +
            "GROUP BY n.channel, n.status")
    List<Object[]> getStatisticsSince(@Param("since") LocalDateTime since);
}
