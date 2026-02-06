package com.rms.repository;

//package com.rms.repository;

import com.rms.entity.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Menu Category Repository
 */
@Repository
public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {

    List<MenuCategory> findByRestaurantIdAndIsActiveTrueOrderByDisplayOrderAsc(Long restaurantId);

    List<MenuCategory> findByRestaurantIdAndParentCategoryIsNullAndIsActiveTrueOrderByDisplayOrderAsc(Long restaurantId);

    Optional<MenuCategory> findByIdAndRestaurantId(Long id, Long restaurantId);

    Boolean existsByRestaurantIdAndName(Long restaurantId, String name);

    @Query("SELECT c FROM MenuCategory c WHERE c.restaurantId = :restaurantId AND c.isFeatured = true AND c.isActive = true ORDER BY c.displayOrder")
    List<MenuCategory> findFeaturedCategories(@Param("restaurantId") Long restaurantId);

    @Query("SELECT COUNT(mi) FROM MenuItem mi WHERE mi.category.id = :categoryId AND mi.isActive = true")
    Long countItemsByCategory(@Param("categoryId") Long categoryId);

    @Query("SELECT c FROM MenuCategory c LEFT JOIN FETCH c.subCategories WHERE c.id = :id")
    Optional<MenuCategory> findByIdWithSubCategories(@Param("id") Long id);
}
