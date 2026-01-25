package com.rms.repository;

import com.rms.entity.ModifierGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Modifier Group Repository
 */
@Repository
public interface ModifierGroupRepository extends JpaRepository<ModifierGroup, Long> {

    List<ModifierGroup> findByRestaurantIdAndIsActiveTrueOrderByDisplayOrderAsc(Long restaurantId);

    Optional<ModifierGroup> findByIdAndRestaurantId(Long id, Long restaurantId);

    Boolean existsByRestaurantIdAndName(Long restaurantId, String name);

    @Query("SELECT mg FROM ModifierGroup mg LEFT JOIN FETCH mg.options WHERE mg.id = :id")
    Optional<ModifierGroup> findByIdWithOptions(@Param("id") Long id);

    @Query("SELECT mg FROM ModifierGroup mg LEFT JOIN FETCH mg.options WHERE mg.restaurantId = :restaurantId AND mg.isActive = true ORDER BY mg.displayOrder")
    List<ModifierGroup> findByRestaurantIdWithOptions(@Param("restaurantId") Long restaurantId);
}
