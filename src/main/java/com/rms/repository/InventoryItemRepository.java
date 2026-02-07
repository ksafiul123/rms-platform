package com.rms.repository;

//package com.rms.repository;

import com.rms.entity.*;
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
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    Optional<InventoryItem> findByIdAndRestaurantId(Long id, Long restaurantId);

    Optional<InventoryItem> findByRestaurantIdAndItemCode(Long restaurantId, String itemCode);

    Page<InventoryItem> findByRestaurantId(Long restaurantId, Pageable pageable);

    List<InventoryItem> findByRestaurantId(Long restaurantId);

    Page<InventoryItem> findByRestaurantIdAndCategory(
            Long restaurantId,
            InventoryItem.InventoryCategory category,
            Pageable pageable
    );

    Page<InventoryItem> findByRestaurantIdAndStatus(
            Long restaurantId,
            InventoryItem.InventoryStatus status,
            Pageable pageable
    );

    List<InventoryItem> findByRestaurantIdAndIsActive(Long restaurantId, Boolean isActive);

    @Query("SELECT i FROM InventoryItem i WHERE i.restaurantId = :restaurantId " +
            "AND i.currentQuantity <= i.minimumQuantity AND i.isActive = true")
    List<InventoryItem> findLowStockItems(@Param("restaurantId") Long restaurantId);

    @Query("SELECT i FROM InventoryItem i WHERE i.restaurantId = :restaurantId " +
            "AND i.currentQuantity <= 0 AND i.isActive = true")
    List<InventoryItem> findOutOfStockItems(@Param("restaurantId") Long restaurantId);

    @Query("SELECT i FROM InventoryItem i WHERE i.restaurantId = :restaurantId " +
            "AND i.expiryDate BETWEEN :startDate AND :endDate")
    List<InventoryItem> findExpiringItems(
            @Param("restaurantId") Long restaurantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    boolean existsByRestaurantIdAndItemCode(Long restaurantId, String itemCode);
}
