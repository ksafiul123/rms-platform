package com.rms.repository;

import com.rms.entity.RestaurantBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Restaurant Branch Repository
 */
@Repository
public interface RestaurantBranchRepository extends JpaRepository<RestaurantBranch, Long> {

    List<RestaurantBranch> findByRestaurantId(Long restaurantId);

    List<RestaurantBranch> findByRestaurantIdAndIsActiveTrue(Long restaurantId);

    Optional<RestaurantBranch> findByBranchCode(String branchCode);

    Optional<RestaurantBranch> findByIdAndRestaurantId(Long id, Long restaurantId);

    Optional<RestaurantBranch> findByRestaurantIdAndIsMainBranchTrue(Long restaurantId);

    Boolean existsByBranchCode(String branchCode);

    @Query("SELECT COUNT(b) FROM RestaurantBranch b WHERE b.restaurantId = :restaurantId AND b.isActive = true")
    Long countActiveByRestaurantId(@Param("restaurantId") Long restaurantId);
}
