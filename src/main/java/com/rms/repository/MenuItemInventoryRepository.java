package com.rms.repository;

import com.rms.entity.MenuItemInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemInventoryRepository extends JpaRepository<MenuItemInventory, Long> {

    List<MenuItemInventory> findByMenuItemId(Long menuItemId);

    List<MenuItemInventory> findByInventoryItemId(Long inventoryItemId);

    Optional<MenuItemInventory> findByMenuItemIdAndInventoryItemId(
            Long menuItemId,
            Long inventoryItemId
    );

    @Query("SELECT m FROM MenuItemInventory m WHERE m.menuItem.id = :menuItemId " +
            "AND m.isOptional = false")
    List<MenuItemInventory> findRequiredIngredients(@Param("menuItemId") Long menuItemId);

    boolean existsByMenuItemIdAndInventoryItemId(Long menuItemId, Long inventoryItemId);

    void deleteByMenuItemId(Long menuItemId);
}
