package com.rms.repository;

import com.rms.entity.ModifierOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Modifier Option Repository
 */
@Repository
public interface ModifierOptionRepository extends JpaRepository<ModifierOption, Long> {

    @Query("SELECT mo FROM ModifierOption mo WHERE mo.modifierGroup.id = :modifierGroupId")
    List<ModifierOption> findByModifierGroupId(@Param("modifierGroupId") Long modifierGroupId);

    @Query("SELECT mo FROM ModifierOption mo WHERE mo.modifierGroup.id = :modifierGroupId AND mo.isAvailable = true")
    List<ModifierOption> findByModifierGroupIdAndIsAvailableTrue(@Param("modifierGroupId") Long modifierGroupId);

    @Query("SELECT mo FROM ModifierOption mo WHERE mo.modifierGroup.id = :modifierGroupId AND mo.isDefault = true")
    Optional<ModifierOption> findByModifierGroupIdAndIsDefaultTrue(@Param("modifierGroupId") Long modifierGroupId);
}
