package com.rms.repository;

import com.rms.entity.MenuPerformanceReport;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MenuPerformanceReportRepository extends JpaRepository<MenuPerformanceReport, Long> {

    List<MenuPerformanceReport> findByRestaurantIdAndReportDateBetween(
            Long restaurantId,
            LocalDate startDate,
            LocalDate endDate
    );

    List<MenuPerformanceReport> findByMenuItemIdAndReportDateBetween(
            Long menuItemId,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query("SELECT mpr FROM MenuPerformanceReport mpr " +
            "WHERE mpr.restaurant.id = :restaurantId " +
            "AND mpr.reportDate BETWEEN :startDate AND :endDate " +
            "ORDER BY mpr.quantitySold DESC")
    List<MenuPerformanceReport> findTopPerformingItems(
            @Param("restaurantId") Long restaurantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query("SELECT mpr FROM MenuPerformanceReport mpr " +
            "WHERE mpr.restaurant.id = :restaurantId " +
            "AND mpr.reportDate BETWEEN :startDate AND :endDate " +
            "ORDER BY mpr.totalRevenue DESC")
    List<MenuPerformanceReport> findTopRevenueItems(
            @Param("restaurantId") Long restaurantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query("SELECT mpr FROM MenuPerformanceReport mpr " +
            "WHERE mpr.restaurant.id = :restaurantId " +
            "AND mpr.reportDate BETWEEN :startDate AND :endDate " +
            "ORDER BY mpr.profitMargin DESC")
    List<MenuPerformanceReport> findMostProfitableItems(
            @Param("restaurantId") Long restaurantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query("SELECT mpr FROM MenuPerformanceReport mpr " +
            "WHERE mpr.restaurant.id = :restaurantId " +
            "AND mpr.reportDate BETWEEN :startDate AND :endDate " +
            "ORDER BY mpr.quantitySold ASC")
    List<MenuPerformanceReport> findSlowMovingItems(
            @Param("restaurantId") Long restaurantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
}
