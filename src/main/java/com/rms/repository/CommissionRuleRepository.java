package com.rms.repository;

import com.rms.entity.CommissionRule;
import com.rms.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommissionRuleRepository extends JpaRepository<CommissionRule, Long> {

    List<CommissionRule> findByRestaurantIdAndIsActiveTrueOrderByPriorityDesc(
            Long restaurantId);

    @Query("SELECT r FROM CommissionRule r " +
            "WHERE r.restaurant.id = :restaurantId " +
            "AND r.isActive = true " +
            "AND :date BETWEEN r.effectiveFrom AND COALESCE(r.effectiveTo, :date) " +
            "ORDER BY r.priority DESC")
    List<CommissionRule> findActiveRulesForDate(
            @Param("restaurantId") Long restaurantId,
            @Param("date") LocalDate date);

    @Query("SELECT r FROM CommissionRule r " +
            "WHERE r.restaurant.id = :restaurantId " +
            "AND r.isActive = true " +
            "AND :date BETWEEN r.effectiveFrom AND COALESCE(r.effectiveTo, :date) " +
            "AND (r.appliesToOrderType = :orderType OR r.appliesToOrderType IS NULL) " +
            "ORDER BY r.priority DESC")
    Optional<CommissionRule> findApplicableRule(
            @Param("restaurantId") Long restaurantId,
            @Param("orderType") Order.OrderType orderType,
            @Param("date") LocalDate date);
}
