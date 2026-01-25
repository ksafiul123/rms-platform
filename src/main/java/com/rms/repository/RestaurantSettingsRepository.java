package com.rms.repository;

//package com.rms.repository;

import com.rms.entity.RestaurantSettings;
import dev.safi.restaurant_management_system.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Restaurant Settings Repository
 */
@Repository
public interface RestaurantSettingsRepository extends JpaRepository<RestaurantSettings, Long> {

    Optional<RestaurantSettings> findByRestaurantId(Long restaurantId);

    Boolean existsByRestaurantId(Long restaurantId);
}

