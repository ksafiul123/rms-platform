package com.rms.repository;

import com.rms.entity.Restaurant;
import com.rms.enums.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Custom Restaurant Repository for complex queries
 */
@Repository
public interface CustomRestaurantRepository {

    Page<Restaurant> findRestaurantsWithFilters(
            String searchTerm,
            SubscriptionStatus subscriptionStatus,
            Boolean isActive,
            Pageable pageable
    );

    List<Restaurant> findRestaurantsByLocation(
            BigDecimal latitude,
            BigDecimal longitude,
            BigDecimal radiusKm
    );
}
