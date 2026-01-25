package dev.safi.restaurant_management_system.repository;

import dev.safi.restaurant_management_system.entity.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Menu Item Repository
 */
@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByRestaurantIdAndIsActiveTrueOrderByDisplayOrderAsc(Long restaurantId);

    List<MenuItem> findByCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(Long categoryId);

    Optional<MenuItem> findByIdAndRestaurantId(Long id, Long restaurantId);

    Optional<MenuItem> findBySkuAndRestaurantId(String sku, Long restaurantId);

    Boolean existsBySkuAndRestaurantId(String sku, Long restaurantId);

    @Query("SELECT mi FROM MenuItem mi WHERE mi.restaurantId = :restaurantId AND mi.isActive = true AND mi.isFeatured = true ORDER BY mi.displayOrder")
    List<MenuItem> findFeaturedItems(@Param("restaurantId") Long restaurantId);

    @Query("SELECT mi FROM MenuItem mi WHERE mi.restaurantId = :restaurantId AND mi.isActive = true AND mi.isBestSeller = true ORDER BY mi.displayOrder")
    List<MenuItem> findBestSellers(@Param("restaurantId") Long restaurantId);

    @Query("SELECT mi FROM MenuItem mi WHERE mi.restaurantId = :restaurantId AND mi.isActive = true AND mi.isAvailable = false")
    List<MenuItem> findUnavailableItems(@Param("restaurantId") Long restaurantId);

    @Query("SELECT mi FROM MenuItem mi WHERE mi.restaurantId = :restaurantId AND mi.stockQuantity IS NOT NULL AND mi.stockQuantity <= mi.lowStockThreshold")
    List<MenuItem> findLowStockItems(@Param("restaurantId") Long restaurantId);

    @Query("SELECT mi FROM MenuItem mi WHERE mi.restaurantId = :restaurantId AND LOWER(mi.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<MenuItem> searchByName(@Param("restaurantId") Long restaurantId, @Param("search") String search, Pageable pageable);

    @Query("SELECT mi FROM MenuItem mi WHERE mi.category.id = :categoryId AND mi.isActive = true AND mi.isAvailable = true ORDER BY mi.displayOrder")
    List<MenuItem> findAvailableItemsByCategory(@Param("categoryId") Long categoryId);

    @Query("SELECT COUNT(mi) FROM MenuItem mi WHERE mi.restaurantId = :restaurantId AND mi.isActive = true")
    Long countActiveItems(@Param("restaurantId") Long restaurantId);

    @Query("SELECT AVG(mi.basePrice) FROM MenuItem mi WHERE mi.restaurantId = :restaurantId AND mi.isActive = true")
    BigDecimal getAveragePrice(@Param("restaurantId") Long restaurantId);

    @Modifying
    @Query("UPDATE MenuItem mi SET mi.isAvailable = :isAvailable WHERE mi.id IN :ids")
    void bulkUpdateAvailability(@Param("ids") List<Long> ids, @Param("isAvailable") Boolean isAvailable);

    @Modifying
    @Query("UPDATE MenuItem mi SET mi.stockQuantity = mi.stockQuantity - :quantity WHERE mi.id = :id AND mi.stockQuantity >= :quantity")
    int decrementStock(@Param("id") Long id, @Param("quantity") Integer quantity);
}
