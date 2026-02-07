package com.rms.repository;

import com.rms.entity.SalesReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesReportRepository extends JpaRepository<SalesReport, Long> {

    Optional<SalesReport> findByRestaurantIdAndReportDate(Long restaurantId, LocalDate reportDate);

    Optional<SalesReport> findByRestaurantIdAndReportDateAndPeriodType(
            Long restaurantId,
            LocalDate reportDate,
            SalesReport.PeriodType periodType
    );

    List<SalesReport> findByRestaurantIdAndReportDateBetween(
            Long restaurantId,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query("SELECT sr FROM SalesReport sr WHERE sr.restaurant.id = :restaurantId " +
            "AND sr.periodType = :periodType ORDER BY sr.reportDate DESC")
    Page<SalesReport> findByRestaurantIdAndPeriodType(
            @Param("restaurantId") Long restaurantId,
            @Param("periodType") SalesReport.PeriodType periodType,
            Pageable pageable
    );

    @Query("SELECT SUM(sr.totalRevenue) FROM SalesReport sr " +
            "WHERE sr.restaurant.id = :restaurantId " +
            "AND sr.reportDate BETWEEN :startDate AND :endDate")
    BigDecimal sumRevenueByRestaurantAndPeriod(
            @Param("restaurantId") Long restaurantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT SUM(sr.totalOrders) FROM SalesReport sr " +
            "WHERE sr.restaurant.id = :restaurantId " +
            "AND sr.reportDate BETWEEN :startDate AND :endDate")
    Long sumOrdersByRestaurantAndPeriod(
            @Param("restaurantId") Long restaurantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
