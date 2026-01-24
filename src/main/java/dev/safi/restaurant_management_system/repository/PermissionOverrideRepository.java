package dev.safi.restaurant_management_system.repository;

import dev.safi.restaurant_management_system.entity.PermissionOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Permission Override Repository
 */
@Repository
public interface PermissionOverrideRepository extends JpaRepository<PermissionOverride, Long> {

    List<PermissionOverride> findByUserId(Long userId);

    List<PermissionOverride> findByUserIdAndIsActiveTrue(Long userId);

    List<PermissionOverride> findByUserIdAndRestaurantId(Long userId, Long restaurantId);

    Optional<PermissionOverride> findByUserIdAndPermissionId(Long userId, Long permissionId);

    @Query("SELECT po FROM PermissionOverride po " +
            "WHERE po.userId = :userId AND po.isActive = true " +
            "AND (po.expiresAt IS NULL OR po.expiresAt > :now)")
    List<PermissionOverride> findActiveByUserId(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now
    );

    @Query("SELECT po FROM PermissionOverride po " +
            "WHERE po.userId = :userId AND po.restaurantId = :restaurantId " +
            "AND po.isActive = true AND (po.expiresAt IS NULL OR po.expiresAt > :now)")
    List<PermissionOverride> findActiveByUserIdAndRestaurantId(
            @Param("userId") Long userId,
            @Param("restaurantId") Long restaurantId,
            @Param("now") LocalDateTime now
    );

    @Query("SELECT po FROM PermissionOverride po " +
            "WHERE po.expiresAt < :now AND po.isActive = true")
    List<PermissionOverride> findExpiredOverrides(@Param("now") LocalDateTime now);
}
