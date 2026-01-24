package dev.safi.restaurant_management_system.repository;

import dev.safi.restaurant_management_system.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Subscription Plan Repository
 */
@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    Optional<SubscriptionPlan> findByNameAndIsActiveTrue(String name);

    List<SubscriptionPlan> findAllByIsActiveTrueOrderByDisplayOrderAsc();

    @Query("SELECT p FROM SubscriptionPlan p WHERE p.isActive = true ORDER BY p.monthlyPrice ASC")
    List<SubscriptionPlan> findActivePlansOrderByPrice();
}
