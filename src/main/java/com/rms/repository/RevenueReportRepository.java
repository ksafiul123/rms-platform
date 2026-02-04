package com.rms.repository;

import com.rms.entity.RevenueReport;
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
public interface RevenueReportRepository extends JpaRepository<RevenueReport, Long> {

    Optional<RevenueReport> findByRestaurantIdAndReportDateAndPeriodType(
            Long restaurantId,
            LocalDate reportDate,
            SalesReport.PeriodType periodType
    );

    List<RevenueReport> findByRestaurantIdAndReportDateBetween(
            Long restaurantId,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query("SELECT rr FROM RevenueReport rr " +
            "WHERE rr.restaurant.id = :restaurantId " +
            "AND rr.periodType = :periodType " +
            "ORDER BY rr.reportDate DESC")
    Page<RevenueReport> findByRestaurantIdAndPeriodType(
            @Param("restaurantId") Long restaurantId,
            @Param("periodType") SalesReport.PeriodType periodType,
            Pageable pageable
    );

    @Query("SELECT SUM(rr.netProfit) FROM RevenueReport rr " +
            "WHERE rr.restaurant.id = :restaurantId " +
            "AND rr.reportDate BETWEEN :startDate AND :endDate")
    BigDecimal sumNetProfitByRestaurantAndPeriod(
            @Param("restaurantId") Long restaurantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT AVG(rr.netProfitMargin) FROM RevenueReport rr " +
            "WHERE rr.restaurant.id = :restaurantId " +
            "AND rr.reportDate BETWEEN :startDate AND :endDate")
    BigDecimal averageNetProfitMarginByRestaurantAndPeriod(
            @Param("restaurantId") Long restaurantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
