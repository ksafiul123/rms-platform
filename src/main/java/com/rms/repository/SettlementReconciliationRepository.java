package com.rms.repository;

import com.rms.entity.SettlementReconciliation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SettlementReconciliationRepository extends JpaRepository<SettlementReconciliation, Long> {

    Optional<SettlementReconciliation> findByReconciliationReference(String reconciliationReference);

    List<SettlementReconciliation> findByRestaurantIdOrderByPerformedAtDesc(Long restaurantId);

    @Query("SELECT r FROM SettlementReconciliation r " +
            "WHERE r.restaurant.id = :restaurantId " +
            "AND r.periodStartDate >= :startDate " +
            "AND r.periodEndDate <= :endDate " +
            "ORDER BY r.periodStartDate DESC")
    List<SettlementReconciliation> findReconciliationsForPeriod(
            @Param("restaurantId") Long restaurantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    List<SettlementReconciliation> findByReconciliationStatus(
            SettlementReconciliation.ReconciliationStatus status);
}
