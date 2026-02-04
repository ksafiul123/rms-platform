package com.rms.repository;

import com.rms.entity.InventoryUsageReport;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface InventoryUsageReportRepository extends JpaRepository<InventoryUsageReport, Long> {

    List<InventoryUsageReport> findByRestaurantIdAndReportDateBetween(
            Long restaurantId,
            LocalDate startDate,
            LocalDate endDate
    );

    List<InventoryUsageReport> findByInventoryItemIdAndReportDateBetween(
            Long inventoryItemId,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query("SELECT iur FROM InventoryUsageReport iur " +
            "WHERE iur.restaurant.id = :restaurantId " +
            "AND iur.reportDate BETWEEN :startDate AND :endDate " +
            "ORDER BY iur.wastagePercentage DESC")
    List<InventoryUsageReport> findHighWastageItems(
            @Param("restaurantId") Long restaurantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query("SELECT iur FROM InventoryUsageReport iur " +
            "WHERE iur.restaurant.id = :restaurantId " +
            "AND iur.reportDate BETWEEN :startDate AND :endDate " +
            "ORDER BY iur.totalCost DESC")
    List<InventoryUsageReport> findTopCostItems(
            @Param("restaurantId") Long restaurantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query("SELECT SUM(iur.wastage) FROM InventoryUsageReport iur " +
            "WHERE iur.restaurant.id = :restaurantId " +
            "AND iur.reportDate BETWEEN :startDate AND :endDate")
    BigDecimal sumWastageByRestaurantAndPeriod(
            @Param("restaurantId") Long restaurantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT SUM(iur.wastageCost) FROM InventoryUsageReport iur " +
            "WHERE iur.restaurant.id = :restaurantId " +
            "AND iur.reportDate BETWEEN :startDate AND :endDate")
    BigDecimal sumWastageCostByRestaurantAndPeriod(
            @Param("restaurantId") Long restaurantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
