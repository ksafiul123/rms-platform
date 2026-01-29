package com.rms.repository;

// OrderStatusHistoryRepository.java
//package com.rms.repository;

import com.rms.entity.Order;
import com.rms.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {

    List<OrderStatusHistory> findByOrderIdOrderByTimestampDesc(Long orderId);

    List<OrderStatusHistory> findByOrderRestaurantIdAndTimestampBetween(
            Long restaurantId, LocalDateTime start, LocalDateTime end);

    List<OrderStatusHistory> findByStatus(Order.OrderStatus status);

    List<OrderStatusHistory> findByUpdatedByIdAndTimestampAfter(
            Long userId, LocalDateTime after);
}


