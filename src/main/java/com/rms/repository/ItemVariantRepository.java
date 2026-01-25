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

    List<ItemVariant> findByMenuItemId(Long menuItemId);

    List<ItemVariant> findByMenuItemIdAndIsAvailableTrue(Long menuItemId);

    Optional<ItemVariant> findByMenuItemIdAndIsDefaultTrue(Long menuItemId);

    @Modifying
    @Query("UPDATE ItemVariant iv SET iv.isDefault = false WHERE iv.menuItem.id = :menuItemId")
    void clearDefaultVariants(@Param("menuItemId") Long menuItemId);
}
