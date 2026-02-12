package com.rms.repository;

import com.rms.entity.ItemIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Item Ingredient Repository
 */
@Repository
public interface ItemIngredientRepository extends JpaRepository<ItemIngredient, Long> {

    @Query("SELECT ii FROM ItemIngredient ii WHERE ii.menuItem.id = :menuItemId")
    List<ItemIngredient> findByMenuItemId(@Param("menuItemId") Long menuItemId);

    @Query("SELECT ii FROM ItemIngredient ii LEFT JOIN FETCH ii.ingredient WHERE ii.menuItem.id = :menuItemId")
    List<ItemIngredient> findByMenuItemIdWithIngredients(@Param("menuItemId") Long menuItemId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ItemIngredient ii WHERE ii.menuItem.id = :menuItemId")
    void deleteByMenuItemId(@Param("menuItemId") Long menuItemId);
}
