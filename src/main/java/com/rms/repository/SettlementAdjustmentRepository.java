package com.rms.repository;

import com.rms.entity.SettlementAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface SettlementAdjustmentRepository extends JpaRepository<SettlementAdjustment, Long> {

    Optional<SettlementAdjustment> findByAdjustmentReference(String adjustmentReference);

    List<SettlementAdjustment> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);

    List<SettlementAdjustment> findBySettlementId(Long settlementId);

    List<SettlementAdjustment> findByStatus(SettlementAdjustment.AdjustmentStatus status);

    @Query("SELECT SUM(a.amount) FROM SettlementAdjustment a " +
            "WHERE a.settlement.id = :settlementId " +
            "AND a.status = 'APPLIED'")
    BigDecimal calculateTotalAdjustments(@Param("settlementId") Long settlementId);
}
