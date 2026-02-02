package com.rms.repository;

import com.rms.entity.DisplayAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DisplayAnalyticsRepository extends JpaRepository<DisplayAnalytics, Long> {

    Optional<DisplayAnalytics> findByRestaurantIdAndAnalyticsDate(
            Long restaurantId, LocalDate date);

    List<DisplayAnalytics> findByRestaurantIdOrderByAnalyticsDateDesc(Long restaurantId);

    @Query("SELECT a FROM DisplayAnalytics a " +
            "WHERE a.restaurant.id = :restaurantId " +
            "AND a.analyticsDate BETWEEN :startDate AND :endDate " +
            "ORDER BY a.analyticsDate DESC")
    List<DisplayAnalytics> findAnalyticsForPeriod(
            @Param("restaurantId") Long restaurantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(a.totalDisplayViews) FROM DisplayAnalytics a " +
            "WHERE a.restaurant.id = :restaurantId " +
            "AND a.analyticsDate BETWEEN :startDate AND :endDate")
    Long sumDisplayViews(
            @Param("restaurantId") Long restaurantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
