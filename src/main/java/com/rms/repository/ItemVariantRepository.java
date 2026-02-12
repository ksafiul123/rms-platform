package com.rms.repository;

import com.rms.entity.ItemVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Item Variant Repository
 */
@Repository
public interface ItemVariantRepository extends JpaRepository<ItemVariant, Long> {

    @Query("SELECT iv FROM ItemVariant iv WHERE iv.menuItem.id = :menuItemId")
    List<ItemVariant> findByMenuItemId(@Param("menuItemId") Long menuItemId);

    @Query("SELECT iv FROM ItemVariant iv WHERE iv.menuItem.id = :menuItemId AND iv.isAvailable = true")
    List<ItemVariant> findByMenuItemIdAndIsAvailableTrue(@Param("menuItemId") Long menuItemId);

    @Query("SELECT iv FROM ItemVariant iv WHERE iv.menuItem.id = :menuItemId AND iv.isDefault = true")
    Optional<ItemVariant> findByMenuItemIdAndIsDefaultTrue(@Param("menuItemId") Long menuItemId);

    @Modifying
    @Query("UPDATE ItemVariant iv SET iv.isDefault = false WHERE iv.menuItem.id = :menuItemId")
    void clearDefaultVariants(@Param("menuItemId") Long menuItemId);
}
