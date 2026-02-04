package com.rms.repository;

import com.rms.entity.CustomerBehaviorReport;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerBehaviorReportRepository extends JpaRepository<CustomerBehaviorReport, Long> {

    Optional<CustomerBehaviorReport> findByCustomerIdAndRestaurantIdAndReportDate(
            Long customerId,
            Long restaurantId,
            LocalDate reportDate
    );

    List<CustomerBehaviorReport> findByRestaurantIdAndReportDateBetween(
            Long restaurantId,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query("SELECT cbr FROM CustomerBehaviorReport cbr " +
            "WHERE cbr.restaurant.id = :restaurantId " +
            "AND cbr.customerSegment = :segment " +
            "AND cbr.reportDate BETWEEN :startDate AND :endDate")
    List<CustomerBehaviorReport> findByRestaurantAndSegment(
            @Param("restaurantId") Long restaurantId,
            @Param("segment") CustomerBehaviorReport.CustomerSegment segment,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT cbr FROM CustomerBehaviorReport cbr " +
            "WHERE cbr.restaurant.id = :restaurantId " +
            "AND cbr.isAtRisk = true " +
            "AND cbr.reportDate = :reportDate")
    List<CustomerBehaviorReport> findAtRiskCustomers(
            @Param("restaurantId") Long restaurantId,
            @Param("reportDate") LocalDate reportDate
    );

    @Query("SELECT cbr FROM CustomerBehaviorReport cbr " +
            "WHERE cbr.restaurant.id = :restaurantId " +
            "AND cbr.reportDate BETWEEN :startDate AND :endDate " +
            "ORDER BY cbr.lifetimeValue DESC")
    List<CustomerBehaviorReport> findTopCustomersByLTV(
            @Param("restaurantId") Long restaurantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query("SELECT COUNT(cbr) FROM CustomerBehaviorReport cbr " +
            "WHERE cbr.restaurant.id = :restaurantId " +
            "AND cbr.customerSegment = :segment " +
            "AND cbr.reportDate = :reportDate")
    Long countByRestaurantAndSegment(
            @Param("restaurantId") Long restaurantId,
            @Param("segment") CustomerBehaviorReport.CustomerSegment segment,
            @Param("reportDate") LocalDate reportDate
    );
}
