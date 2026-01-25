package com.rms.repository;

import com.rms.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Restaurant Repository
 */
@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    Optional<Restaurant> findByRestaurantCode(String restaurantCode);

    Optional<Restaurant> findByEmail(String email);

    Boolean existsByEmail(String email);

    Boolean existsByRestaurantCode(String restaurantCode);

    @Query("SELECT r FROM Restaurant r WHERE r.isActive = true AND r.subscriptionStatus = 'ACTIVE'")
    java.util.List<Restaurant> findAllActiveRestaurants();

    @Query("SELECT r FROM Restaurant r WHERE r.subscriptionExpiry < :currentDate AND r.subscriptionStatus = 'ACTIVE'")
    java.util.List<Restaurant> findExpiredSubscriptions(@Param("currentDate") LocalDateTime currentDate);
}
