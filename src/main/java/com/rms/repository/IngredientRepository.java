package com.rms.repository;

import com.rms.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Ingredient Repository
 */
@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    List<Ingredient> findByRestaurantIdAndIsActiveTrueOrderByNameAsc(Long restaurantId);

    Optional<Ingredient> findByIdAndRestaurantId(Long id, Long restaurantId);

    Optional<Ingredient> findBySkuAndRestaurantId(String sku, Long restaurantId);

    Boolean existsBySkuAndRestaurantId(String sku, Long restaurantId);

    @Query("SELECT i FROM Ingredient i WHERE i.restaurantId = :restaurantId AND i.currentStock <= i.minStock AND i.isActive = true")
    List<Ingredient> findLowStockIngredients(@Param("restaurantId") Long restaurantId);

    @Modifying
    @Query("UPDATE Ingredient i SET i.currentStock = i.currentStock - :quantity WHERE i.id = :id AND i.currentStock >= :quantity")
    int decrementStock(@Param("id") Long id, @Param("quantity") BigDecimal quantity);
}
