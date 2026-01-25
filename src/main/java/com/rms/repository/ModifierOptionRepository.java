package com.rms.repository;

import com.rms.entity.ModifierOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Modifier Option Repository
 */
@Repository
public interface ModifierOptionRepository extends JpaRepository<ModifierOption, Long> {

    List<ModifierOption> findByModifierGroupId(Long modifierGroupId);

    List<ModifierOption> findByModifierGroupIdAndIsAvailableTrue(Long modifierGroupId);

    Optional<ModifierOption> findByModifierGroupIdAndIsDefaultTrue(Long modifierGroupId);
}
