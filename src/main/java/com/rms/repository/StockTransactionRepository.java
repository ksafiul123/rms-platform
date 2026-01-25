package com.rms.repository;

import com.rms.entity.StockTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {

    Page<StockTransaction> findByRestaurantId(Long restaurantId, Pageable pageable);

    Page<StockTransaction> findByInventoryItemId(Long inventoryItemId, Pageable pageable);

    Page<StockTransaction> findByRestaurantIdAndTransactionType(
            Long restaurantId,
            StockTransaction.TransactionType transactionType,
            Pageable pageable
    );

    @Query("SELECT s FROM StockTransaction s WHERE s.restaurantId = :restaurantId " +
            "AND s.createdAt BETWEEN :startDate AND :endDate")
    List<StockTransaction> findByRestaurantIdAndDateRange(
            @Param("restaurantId") Long restaurantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT s FROM StockTransaction s WHERE s.inventoryItem.id = :inventoryItemId " +
            "ORDER BY s.createdAt DESC")
    List<StockTransaction> findRecentTransactions(
            @Param("inventoryItemId") Long inventoryItemId,
            Pageable pageable
    );

    @Query("SELECT SUM(s.totalCost) FROM StockTransaction s " +
            "WHERE s.restaurantId = :restaurantId " +
            "AND s.transactionType = 'PURCHASE' " +
            "AND s.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal calculatePurchaseCost(
            @Param("restaurantId") Long restaurantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
