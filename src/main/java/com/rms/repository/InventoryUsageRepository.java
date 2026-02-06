package com.rms.repository;

import com.rms.entity.InventoryUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InventoryUsageRepository extends JpaRepository<InventoryUsage, Long> {

    @Query("SELECT iu FROM InventoryUsage iu WHERE iu.restaurant.id = :restaurantId " +
            "AND iu.analysisDate BETWEEN :startDate AND :endDate")
    List<InventoryUsage> findByRestaurantIdAndDateRange(
            @Param("restaurantId") Long restaurantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    List<InventoryUsage> findByRestaurantIdAndAnalysisDate(Long restaurantId, LocalDate analysisDate);

    @Query("SELECT MAX(iu.analysisDate) FROM InventoryUsage iu WHERE iu.inventoryItem.id = :inventoryItemId")
    LocalDate findLastUsageDate(@Param("inventoryItemId") Long inventoryItemId);
}
