package com.rms.repository;

import com.rms.entity.RevenueAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RevenueAnalyticsRepository extends JpaRepository<RevenueAnalytics, Long> {

    @Query("SELECT ra FROM RevenueAnalytics ra WHERE ra.restaurant.id = :restaurantId " +
            "AND ra.analysisDate BETWEEN :startDate AND :endDate")
    List<RevenueAnalytics> findByRestaurantIdAndDateRange(
            @Param("restaurantId") Long restaurantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
