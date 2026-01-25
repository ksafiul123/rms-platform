package com.rms.repository;

import com.rms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * User Repository with multi-tenant support
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndIsActiveTrue(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByIdAndRestaurantId(Long id, Long restaurantId);

    Boolean existsByEmail(String email);

    Boolean existsByPhoneNumber(String phoneNumber);

    @Query("SELECT u FROM User u WHERE u.restaurantId = :restaurantId AND u.isActive = true")
    java.util.List<User> findAllByRestaurantId(@Param("restaurantId") Long restaurantId);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.restaurantId = :restaurantId")
    java.util.List<User> findByRestaurantIdAndRole(
            @Param("restaurantId") Long restaurantId,
            @Param("roleName") String roleName
    );

    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :loginTime WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime);
}






