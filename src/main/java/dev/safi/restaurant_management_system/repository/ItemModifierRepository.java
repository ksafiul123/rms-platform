package dev.safi.restaurant_management_system.repository;

import dev.safi.restaurant_management_system.entity.ItemModifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Item Modifier Repository
 */
@Repository
public interface ItemModifierRepository extends JpaRepository<ItemModifier, Long> {

    List<ItemModifier> findByMenuItemId(Long menuItemId);

    @Query("SELECT im FROM ItemModifier im LEFT JOIN FETCH im.modifierGroup mg LEFT JOIN FETCH mg.options WHERE im.menuItem.id = :menuItemId ORDER BY im.displayOrder")
    List<ItemModifier> findByMenuItemIdWithGroups(@Param("menuItemId") Long menuItemId);

    Boolean existsByMenuItemIdAndModifierGroupId(Long menuItemId, Long modifierGroupId);

    void deleteByMenuItemIdAndModifierGroupId(Long menuItemId, Long modifierGroupId);
}
