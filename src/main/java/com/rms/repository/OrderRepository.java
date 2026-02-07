package com.rms.repository;

//package com.rms.repository;

import com.rms.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByIdAndRestaurantId(Long id, Long restaurantId);

    Optional<Order> findByOrderNumberAndRestaurantId(String orderNumber, Long restaurantId);

    Page<Order> findByRestaurantId(Long restaurantId, Pageable pageable);

    Page<Order> findByRestaurantIdAndStatus(Long restaurantId, Order.OrderStatus status, Pageable pageable);

    Page<Order> findByRestaurantIdAndOrderType(Long restaurantId, Order.OrderType orderType, Pageable pageable);

    Page<Order> findByCustomerId(Long customerId, Pageable pageable);

    List<Order> findByCustomerId(Long customerId);

    Page<Order> findByCustomerIdAndRestaurantId(Long customerId, Long restaurantId, Pageable pageable);

    List<Order> findByRestaurantIdAndStatusInOrderByPriorityDescCreatedAtAsc(
            Long restaurantId, List<Order.OrderStatus> statuses);

    Page<Order> findByDeliveryManId(Long deliveryManId, Pageable pageable);

    List<Order> findByRestaurantIdAndCreatedAtBetween(
            Long restaurantId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT o FROM Order o WHERE o.restaurantId = :restaurantId " +
            "AND o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findByRestaurantIdAndDateRange(
            @Param("restaurantId") Long restaurantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT o FROM Order o WHERE o.restaurantId = :restaurantId " +
            "AND o.status IN :statuses")
    List<Order> findByRestaurantIdAndStatusIn(
            @Param("restaurantId") Long restaurantId,
            @Param("statuses") List<Order.OrderStatus> statuses
    );

    @Query("SELECT o FROM Order o WHERE o.restaurantId = :restaurantId " +
            "AND o.status IN ('COMPLETED', 'DELIVERED') " +
            "AND o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findCompletedByRestaurantIdAndDateRange(
            @Param("restaurantId") Long restaurantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COUNT(o) FROM Order o WHERE o.restaurantId = :restaurantId " +
            "AND o.status = :status")
    Long countByRestaurantIdAndStatus(
            @Param("restaurantId") Long restaurantId,
            @Param("status") Order.OrderStatus status
    );

    @Query("SELECT COUNT(o) FROM Order o WHERE o.customerId = :customerId")
    int countByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
            "WHERE o.customerId = :customerId AND o.paymentStatus = 'PAID'")
    java.math.BigDecimal calculateTotalSpentByCustomer(@Param("customerId") Long customerId);

    @Query("SELECT COALESCE(AVG(o.totalAmount), 0) FROM Order o " +
            "WHERE o.customerId = :customerId AND o.paymentStatus = 'PAID'")
    java.math.BigDecimal calculateAvgOrderValueByCustomer(@Param("customerId") Long customerId);

    @Query("SELECT MAX(o.createdAt) FROM Order o WHERE o.customerId = :customerId")
    LocalDateTime findLastOrderDateByCustomer(@Param("customerId") Long customerId);

    @Query("SELECT o FROM Order o WHERE o.restaurantId = :restaurantId " +
            "AND o.status IN ('CONFIRMED', 'PREPARING', 'READY', 'OUT_FOR_DELIVERY', 'DELIVERED', 'COMPLETED') " +
            "ORDER BY o.priority DESC, o.createdAt ASC")
    List<Order> findActiveOrdersForDisplay(@Param("restaurantId") Long restaurantId);

    boolean existsByOrderNumber(String orderNumber);
}
