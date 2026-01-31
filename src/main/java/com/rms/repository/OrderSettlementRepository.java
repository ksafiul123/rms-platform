package com.rms.repository;

// OrderSettlementRepository.java
//package com.rms.repository;

import com.rms.entity.OrderSettlement;
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
public interface OrderSettlementRepository extends JpaRepository<OrderSettlement, Long> {

    Optional<OrderSettlement> findBySettlementReference(String settlementReference);

    Optional<OrderSettlement> findByOrderId(Long orderId);

    Page<OrderSettlement> findByRestaurantIdOrderBySettlementDateDesc(
            Long restaurantId, Pageable pageable);

    List<OrderSettlement> findByRestaurantIdAndSettlementStatus(
            Long restaurantId, OrderSettlement.SettlementStatus status);

    @Query("SELECT s FROM OrderSettlement s " +
            "WHERE s.restaurant.id = :restaurantId " +
            "AND s.settlementStatus = 'PENDING' " +
            "AND s.settlementDate BETWEEN :startDate AND :endDate " +
            "ORDER BY s.settlementDate ASC")
    List<OrderSettlement> findPendingSettlementsForPeriod(
            @Param("restaurantId") Long restaurantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(s.netAmount) FROM OrderSettlement s " +
            "WHERE s.restaurant.id = :restaurantId " +
            "AND s.settlementStatus = 'PENDING'")
    BigDecimal calculatePendingAmount(@Param("restaurantId") Long restaurantId);

    @Query("SELECT COUNT(s) FROM OrderSettlement s " +
            "WHERE s.restaurant.id = :restaurantId " +
            "AND s.settlementStatus = :status")
    Long countByRestaurantAndStatus(
            @Param("restaurantId") Long restaurantId,
            @Param("status") OrderSettlement.SettlementStatus status);
}

