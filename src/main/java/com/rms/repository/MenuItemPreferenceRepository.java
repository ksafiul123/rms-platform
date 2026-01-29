package com.rms.repository;

import com.rms.entity.MenuItemPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemPreferenceRepository extends JpaRepository<MenuItemPreference, Long> {

    Optional<MenuItemPreference> findByCustomerIdAndMenuItemId(Long customerId, Long menuItemId);

    List<MenuItemPreference> findByCustomerId(Long customerId);

    List<MenuItemPreference> findByMenuItemId(Long menuItemId);

    boolean existsByCustomerIdAndMenuItemId(Long customerId, Long menuItemId);

    void deleteByCustomerIdAndMenuItemId(Long customerId, Long menuItemId);

    @Query("SELECT m FROM MenuItemPreference m WHERE m.customerId = :customerId " +
            "AND m.menuItemId IN :menuItemIds")
    List<MenuItemPreference> findByCustomerIdAndMenuItemIdIn(
            @Param("customerId") Long customerId,
            @Param("menuItemIds") List<Long> menuItemIds
    );
}
