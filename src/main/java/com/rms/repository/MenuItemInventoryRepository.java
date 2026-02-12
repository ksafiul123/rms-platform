package com.rms.repository;

import com.rms.entity.MenuItemInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemInventoryRepository extends JpaRepository<MenuItemInventory, Long> {

    @Query("SELECT m FROM MenuItemInventory m WHERE m.menuItem.id = :menuItemId")
    List<MenuItemInventory> findByMenuItemId(@Param("menuItemId") Long menuItemId);

    @Query("SELECT m FROM MenuItemInventory m WHERE m.inventoryItem.id = :inventoryItemId")
    List<MenuItemInventory> findByInventoryItemId(@Param("inventoryItemId") Long inventoryItemId);

    @Query("SELECT m FROM MenuItemInventory m WHERE m.menuItem.id = :menuItemId " +
            "AND m.inventoryItem.id = :inventoryItemId")
    Optional<MenuItemInventory> findByMenuItemIdAndInventoryItemId(
            @Param("menuItemId") Long menuItemId,
            @Param("inventoryItemId") Long inventoryItemId
    );

    @Query("SELECT m FROM MenuItemInventory m WHERE m.menuItem.id = :menuItemId " +
            "AND m.isOptional = false")
    List<MenuItemInventory> findRequiredIngredients(@Param("menuItemId") Long menuItemId);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM MenuItemInventory m " +
            "WHERE m.menuItem.id = :menuItemId AND m.inventoryItem.id = :inventoryItemId")
    boolean existsByMenuItemIdAndInventoryItemId(
            @Param("menuItemId") Long menuItemId,
            @Param("inventoryItemId") Long inventoryItemId
    );

    @Modifying
    @Transactional
    @Query("DELETE FROM MenuItemInventory m WHERE m.menuItem.id = :menuItemId")
    void deleteByMenuItemId(@Param("menuItemId") Long menuItemId);
}
