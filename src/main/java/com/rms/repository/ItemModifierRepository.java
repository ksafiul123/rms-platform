package com.rms.repository;

import com.rms.entity.ItemModifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Item Modifier Repository
 */
@Repository
public interface ItemModifierRepository extends JpaRepository<ItemModifier, Long> {

    @Query("SELECT im FROM ItemModifier im WHERE im.menuItem.id = :menuItemId")
    List<ItemModifier> findByMenuItemId(@Param("menuItemId") Long menuItemId);

    @Query("SELECT im FROM ItemModifier im LEFT JOIN FETCH im.modifierGroup WHERE im.menuItem.id = :menuItemId ORDER BY im.displayOrder")
    List<ItemModifier> findByMenuItemIdWithGroups(@Param("menuItemId") Long menuItemId);

    @Query("SELECT CASE WHEN COUNT(im) > 0 THEN true ELSE false END FROM ItemModifier im WHERE im.menuItem.id = :menuItemId AND im.modifierGroup.id = :modifierGroupId")
    Boolean existsByMenuItemIdAndModifierGroupId(@Param("menuItemId") Long menuItemId, @Param("modifierGroupId") Long modifierGroupId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ItemModifier im WHERE im.menuItem.id = :menuItemId AND im.modifierGroup.id = :modifierGroupId")
    void deleteByMenuItemIdAndModifierGroupId(@Param("menuItemId") Long menuItemId, @Param("modifierGroupId") Long modifierGroupId);
}
