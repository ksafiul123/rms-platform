package com.rms.repository;

import com.rms.entity.RestaurantPayout;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantPayoutRepository extends JpaRepository<RestaurantPayout, Long> {

    Optional<RestaurantPayout> findByPayoutReference(String payoutReference);

    Page<RestaurantPayout> findByRestaurantIdOrderByCreatedAtDesc(
            Long restaurantId, Pageable pageable);

    List<RestaurantPayout> findByPayoutStatus(RestaurantPayout.PayoutStatus status);

    @Query("SELECT p FROM RestaurantPayout p " +
            "WHERE p.restaurant.id = :restaurantId " +
            "AND p.payoutDate BETWEEN :startDate AND :endDate " +
            "ORDER BY p.payoutDate DESC")
    List<RestaurantPayout> findPayoutHistory(
            @Param("restaurantId") Long restaurantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT p FROM RestaurantPayout p " +
            "WHERE p.payoutStatus = 'PENDING_APPROVAL' " +
            "ORDER BY p.initiatedAt ASC")
    List<RestaurantPayout> findPendingApprovals();

    @Query("SELECT COUNT(p) FROM RestaurantPayout p " +
            "WHERE p.restaurant.id = :restaurantId " +
            "AND p.payoutStatus = 'COMPLETED'")
    Long countCompletedPayouts(@Param("restaurantId") Long restaurantId);
}
