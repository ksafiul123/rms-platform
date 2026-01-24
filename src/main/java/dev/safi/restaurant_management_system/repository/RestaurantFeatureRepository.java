package dev.safi.restaurant_management_system.repository;

import dev.safi.restaurant_management_system.entity.FeatureName;
import dev.safi.restaurant_management_system.entity.RestaurantFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Restaurant Feature Repository
 */
@Repository
public interface RestaurantFeatureRepository extends JpaRepository<RestaurantFeature, Long> {

    List<RestaurantFeature> findByRestaurantId(Long restaurantId);

    List<RestaurantFeature> findByRestaurantIdAndIsEnabledTrue(Long restaurantId);

    Optional<RestaurantFeature> findByRestaurantIdAndFeatureName(Long restaurantId, FeatureName featureName);

    Boolean existsByRestaurantIdAndFeatureNameAndIsEnabledTrue(Long restaurantId, FeatureName featureName);

    @Query("SELECT f.featureName FROM RestaurantFeature f WHERE f.restaurantId = :restaurantId AND f.isEnabled = true")
    List<FeatureName> findEnabledFeatureNames(@Param("restaurantId") Long restaurantId);

    @Modifying
    @Query("UPDATE RestaurantFeature f SET f.isEnabled = :isEnabled, f.enabledAt = :enabledAt, f.enabledBy = :enabledBy WHERE f.restaurantId = :restaurantId AND f.featureName = :featureName")
    void updateFeatureStatus(
            @Param("restaurantId") Long restaurantId,
            @Param("featureName") FeatureName featureName,
            @Param("isEnabled") Boolean isEnabled,
            @Param("enabledAt") LocalDateTime enabledAt,
            @Param("enabledBy") Long enabledBy
    );
}
