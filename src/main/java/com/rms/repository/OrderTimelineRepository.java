package com.rms.repository;

import com.rms.entity.OrderTimeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderTimelineRepository extends JpaRepository<OrderTimeline, Long> {

    List<OrderTimeline> findByOrderIdOrderByTimestampDesc(Long orderId);

    List<OrderTimeline> findByOrderIdAndIsMilestoneTrue(Long orderId);

    @Query("SELECT t FROM OrderTimeline t " +
            "WHERE t.order.id = :orderId " +
            "AND t.eventType = :eventType")
    List<OrderTimeline> findByOrderIdAndEventType(
            @Param("orderId") Long orderId,
            @Param("eventType") OrderTimeline.EventType eventType);

    @Query("SELECT t FROM OrderTimeline t " +
            "WHERE t.order.customerId = :customerId " +
            "AND t.timestamp >= :since " +
            "ORDER BY t.timestamp DESC")
    List<OrderTimeline> findRecentEventsForCustomer(
            @Param("customerId") Long customerId,
            @Param("since") LocalDateTime since);
}
