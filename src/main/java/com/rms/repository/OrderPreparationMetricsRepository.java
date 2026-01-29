package com.rms.repository;

import com.rms.entity.OrderPreparationMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderPreparationMetricsRepository extends JpaRepository<OrderPreparationMetrics, Long> {

    Optional<OrderPreparationMetrics> findByOrderId(Long orderId);

    @Query("SELECT m FROM OrderPreparationMetrics m " +
            "WHERE m.order.restaurantId = :restaurantId " +
            "AND DATE(m.confirmedAt) = :date")
    List<OrderPreparationMetrics> findByRestaurantAndDate(
            @Param("restaurantId") Long restaurantId,
            @Param("date") LocalDate date);

    @Query("SELECT m FROM OrderPreparationMetrics m " +
            "WHERE m.order.restaurantId = :restaurantId " +
            "AND m.confirmedAt BETWEEN :start AND :end")
    List<OrderPreparationMetrics> findByRestaurantAndDateRange(
            @Param("restaurantId") Long restaurantId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT AVG(m.actualPreparationTime) FROM OrderPreparationMetrics m " +
            "WHERE m.order.restaurantId = :restaurantId " +
            "AND DATE(m.confirmedAt) = :date")
    Double getAveragePreparationTime(
            @Param("restaurantId") Long restaurantId,
            @Param("date") LocalDate date);

    @Query("SELECT COUNT(m) FROM OrderPreparationMetrics m " +
            "WHERE m.order.restaurantId = :restaurantId " +
            "AND DATE(m.confirmedAt) = :date " +
            "AND m.wasOnTime = true")
    long countOnTimeOrders(
            @Param("restaurantId") Long restaurantId,
            @Param("date") LocalDate date);

    @Query("SELECT m FROM OrderPreparationMetrics m " +
            "WHERE m.order.restaurantId = :restaurantId " +
            "AND m.wasOnTime = false " +
            "AND m.confirmedAt >= :since " +
            "ORDER BY m.delayMinutes DESC")
    List<OrderPreparationMetrics> findDelayedOrders(
            @Param("restaurantId") Long restaurantId,
            @Param("since") LocalDateTime since);
}
