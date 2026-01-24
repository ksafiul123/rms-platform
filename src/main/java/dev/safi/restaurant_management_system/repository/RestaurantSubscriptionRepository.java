package dev.safi.restaurant_management_system.repository;

import dev.safi.restaurant_management_system.entity.RestaurantSubscription;
import dev.safi.restaurant_management_system.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Restaurant Subscription Repository
 */
@Repository
public interface RestaurantSubscriptionRepository extends JpaRepository<RestaurantSubscription, Long> {

    Optional<RestaurantSubscription> findByRestaurantIdAndStatus(Long restaurantId, SubscriptionStatus status);

    @Query("SELECT s FROM RestaurantSubscription s WHERE s.restaurantId = :restaurantId ORDER BY s.createdAt DESC")
    List<RestaurantSubscription> findByRestaurantIdOrderByCreatedAtDesc(@Param("restaurantId") Long restaurantId);

    @Query("SELECT s FROM RestaurantSubscription s WHERE s.restaurantId = :restaurantId ORDER BY s.createdAt DESC")
    Optional<RestaurantSubscription> findLatestByRestaurantId(@Param("restaurantId") Long restaurantId);

    @Query("SELECT s FROM RestaurantSubscription s WHERE s.status = 'ACTIVE' AND s.expiryDate < :currentDate")
    List<RestaurantSubscription> findExpiredSubscriptions(@Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT s FROM RestaurantSubscription s WHERE s.status = 'ACTIVE' AND s.expiryDate BETWEEN :startDate AND :endDate")
    List<RestaurantSubscription> findExpiringSubscriptions(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COUNT(s) FROM RestaurantSubscription s WHERE s.status = :status")
    Long countByStatus(@Param("status") SubscriptionStatus status);
}
