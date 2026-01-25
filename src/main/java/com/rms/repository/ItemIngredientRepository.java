package com.rms.repository;

import com.rms.entity.ItemIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Item Ingredient Repository
 */
@Repository
public interface ItemIngredientRepository extends JpaRepository<ItemIngredient, Long> {

    List<ItemIngredient> findByMenuItemId(Long menuItemId);

    @Query("SELECT ii FROM ItemIngredient ii LEFT JOIN FETCH ii.ingredient WHERE ii.menuItem.id = :menuItemId")
    List<ItemIngredient> findByMenuItemIdWithIngredients(@Param("menuItemId") Long menuItemId);

    void deleteByMenuItemId(Long menuItemId);
}
