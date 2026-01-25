package com.rms.repository;

import com.rms.entity.UserCustomRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * User Custom Role Repository
 */
@Repository
public interface UserCustomRoleRepository extends JpaRepository<UserCustomRole, Long> {

    List<UserCustomRole> findByUserId(Long userId);

    List<UserCustomRole> findByUserIdAndIsActiveTrue(Long userId);

    List<UserCustomRole> findByUserIdAndRestaurantId(Long userId, Long restaurantId);

    List<UserCustomRole> findByRestaurantId(Long restaurantId);

    Optional<UserCustomRole> findByUserIdAndCustomRoleId(Long userId, Long customRoleId);

    Boolean existsByUserIdAndCustomRoleIdAndIsActiveTrue(Long userId, Long customRoleId);

    @Query("SELECT ucr FROM UserCustomRole ucr " +
            "LEFT JOIN FETCH ucr.customRole cr " +
            "LEFT JOIN FETCH cr.permissions " +
            "WHERE ucr.userId = :userId AND ucr.isActive = true")
    List<UserCustomRole> findByUserIdWithPermissions(@Param("userId") Long userId);

    @Query("SELECT ucr FROM UserCustomRole ucr " +
            "LEFT JOIN FETCH ucr.customRole cr " +
            "LEFT JOIN FETCH cr.permissions " +
            "WHERE ucr.userId = :userId AND ucr.restaurantId = :restaurantId AND ucr.isActive = true")
    List<UserCustomRole> findByUserIdAndRestaurantIdWithPermissions(
            @Param("userId") Long userId,
            @Param("restaurantId") Long restaurantId
    );
}
