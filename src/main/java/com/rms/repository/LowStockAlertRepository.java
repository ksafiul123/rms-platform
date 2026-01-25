package com.rms.repository;

import com.rms.entity.LowStockAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LowStockAlertRepository extends JpaRepository<LowStockAlert, Long> {

    Page<LowStockAlert> findByRestaurantId(Long restaurantId, Pageable pageable);

    Page<LowStockAlert> findByRestaurantIdAndStatus(
            Long restaurantId,
            LowStockAlert.AlertStatus status,
            Pageable pageable
    );

    List<LowStockAlert> findByRestaurantIdAndStatusAndAlertType(
            Long restaurantId,
            LowStockAlert.AlertStatus status,
            LowStockAlert.AlertType alertType
    );

    Optional<LowStockAlert> findByInventoryItemIdAndStatus(
            Long inventoryItemId,
            LowStockAlert.AlertStatus status
    );

    @Query("SELECT COUNT(a) FROM LowStockAlert a WHERE a.restaurantId = :restaurantId " +
            "AND a.status = 'ACTIVE'")
    Long countActiveAlerts(@Param("restaurantId") Long restaurantId);

    @Query("SELECT a FROM LowStockAlert a WHERE a.restaurantId = :restaurantId " +
            "AND a.status = 'ACTIVE' ORDER BY a.createdAt DESC")
    List<LowStockAlert> findActiveAlerts(@Param("restaurantId") Long restaurantId);
}
