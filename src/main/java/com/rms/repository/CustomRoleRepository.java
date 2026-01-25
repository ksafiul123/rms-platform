package com.rms.repository;

import com.rms.entity.CustomRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Custom Role Repository
 */
@Repository
public interface CustomRoleRepository extends JpaRepository<CustomRole, Long> {

    List<CustomRole> findByRestaurantId(Long restaurantId);

    List<CustomRole> findByRestaurantIdAndIsActiveTrue(Long restaurantId);

    Optional<CustomRole> findByRestaurantIdAndName(Long restaurantId, String name);

    Optional<CustomRole> findByIdAndRestaurantId(Long id, Long restaurantId);

    Boolean existsByRestaurantIdAndName(Long restaurantId, String name);

    @Query("SELECT cr FROM CustomRole cr LEFT JOIN FETCH cr.permissions WHERE cr.id = :id")
    Optional<CustomRole> findByIdWithPermissions(@Param("id") Long id);

    @Query("SELECT cr FROM CustomRole cr LEFT JOIN FETCH cr.permissions WHERE cr.restaurantId = :restaurantId AND cr.isActive = true")
    List<CustomRole> findByRestaurantIdWithPermissions(@Param("restaurantId") Long restaurantId);

    @Query("SELECT COUNT(ucr) FROM UserCustomRole ucr WHERE ucr.customRole.id = :customRoleId AND ucr.isActive = true")
    Long countUsersWithRole(@Param("customRoleId") Long customRoleId);
}
