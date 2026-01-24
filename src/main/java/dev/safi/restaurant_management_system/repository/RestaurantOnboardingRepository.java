package dev.safi.restaurant_management_system.repository;

import dev.safi.restaurant_management_system.entity.OnboardingStatus;
import dev.safi.restaurant_management_system.entity.RestaurantOnboarding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Restaurant Onboarding Repository
 */
@Repository
public interface RestaurantOnboardingRepository extends JpaRepository<RestaurantOnboarding, Long> {

    Optional<RestaurantOnboarding> findByRestaurantId(Long restaurantId);

    List<RestaurantOnboarding> findBySalesmanId(Long salesmanId);

    List<RestaurantOnboarding> findByStatus(OnboardingStatus status);

    @Query("SELECT o FROM RestaurantOnboarding o WHERE o.salesmanId = :salesmanId AND o.status = :status")
    List<RestaurantOnboarding> findBySalesmanIdAndStatus(
            @Param("salesmanId") Long salesmanId,
            @Param("status") OnboardingStatus status
    );

    @Query("SELECT COUNT(o) FROM RestaurantOnboarding o WHERE o.status = :status")
    Long countByStatus(@Param("status") OnboardingStatus status);

    @Query("SELECT o FROM RestaurantOnboarding o WHERE o.status != 'COMPLETED' AND o.createdAt < :date")
    List<RestaurantOnboarding> findPendingOnboardingBefore(@Param("date") LocalDateTime date);
}
