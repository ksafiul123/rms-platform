package com.rms.repository;

import com.rms.entity.PopularItem;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PopularItemRepository extends JpaRepository<PopularItem, Long> {

    @Query("SELECT p FROM PopularItem p WHERE p.restaurant.id = :restaurantId " +
            "AND p.analysisDate BETWEEN :startDate AND :endDate " +
            "ORDER BY p.totalRevenue DESC")
    List<PopularItem> findTopByRestaurantIdAndPeriod(
            @Param("restaurantId") Long restaurantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    default List<PopularItem> findTopByRestaurantIdAndPeriod(
            Long restaurantId, LocalDate startDate, LocalDate endDate, int limit) {
        return findTopByRestaurantIdAndPeriod(
                restaurantId, startDate, endDate, PageRequest.of(0, limit));
    }

    @Query("SELECT p FROM PopularItem p WHERE p.restaurant.id = :restaurantId " +
            "AND p.analysisDate BETWEEN :startDate AND :endDate")
    List<PopularItem> findByRestaurantIdAndPeriod(
            @Param("restaurantId") Long restaurantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT p FROM PopularItem p WHERE p.menuItem.id = :menuItemId " +
            "AND p.analysisDate BETWEEN :startDate AND :endDate")
    List<PopularItem> findByMenuItemIdAndDateRange(
            @Param("menuItemId") Long menuItemId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    Optional<PopularItem> findByRestaurantIdAndMenuItemIdAndAnalysisDate(
            Long restaurantId, Long menuItemId, LocalDate analysisDate);
}
