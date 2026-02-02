package com.rms.repository;

import com.rms.entity.OrderDisplaySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderDisplaySnapshotRepository extends JpaRepository<OrderDisplaySnapshot, Long> {

    Optional<OrderDisplaySnapshot> findByOrderId(Long orderId);

    List<OrderDisplaySnapshot> findByRestaurantIdOrderByDisplayPositionAsc(Long restaurantId);

    @Query("SELECT s FROM OrderDisplaySnapshot s " +
            "WHERE s.restaurant.id = :restaurantId " +
            "AND s.displayStatus != 'HIDDEN' " +
            "ORDER BY s.priority DESC, s.displayPosition ASC")
    List<OrderDisplaySnapshot> findActiveByRestaurantId(@Param("restaurantId") Long restaurantId);

    @Query("SELECT s FROM OrderDisplaySnapshot s " +
            "WHERE s.restaurant.id = :restaurantId " +
            "AND s.displayStatus = :status " +
            "ORDER BY s.priority DESC, s.createdAt ASC")
    List<OrderDisplaySnapshot> findByRestaurantIdAndStatus(
            @Param("restaurantId") Long restaurantId,
            @Param("status") OrderDisplaySnapshot.DisplayStatus status);

    @Query("SELECT s FROM OrderDisplaySnapshot s " +
            "WHERE s.isHighlighted = true " +
            "AND s.highlightedAt < :expiredBefore")
    List<OrderDisplaySnapshot> findExpiredHighlights(@Param("expiredBefore") LocalDateTime expiredBefore);

    @Modifying
    @Query("DELETE FROM OrderDisplaySnapshot s " +
            "WHERE s.displayStatus = 'COLLECTED' " +
            "AND s.lastUpdated < :before")
    void deleteOldCollectedSnapshots(@Param("before") LocalDateTime before);

    @Query("SELECT COUNT(s) FROM OrderDisplaySnapshot s " +
            "WHERE s.restaurant.id = :restaurantId " +
            "AND s.displayStatus IN ('PREPARING', 'READY')")
    Long countActiveOrders(@Param("restaurantId") Long restaurantId);
}
