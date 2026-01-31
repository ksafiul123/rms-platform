package com.rms.repository;

// PaymentRepository.java
//package com.rms.repository;

import com.rms.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentReference(String paymentReference);

    Optional<Payment> findByProviderTransactionId(String providerTransactionId);

    List<Payment> findByOrderId(Long orderId);

    Page<Payment> findByCustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);

    @Query("SELECT p FROM Payment p " +
            "WHERE p.restaurant.id = :restaurantId " +
            "AND p.status = :status " +
            "ORDER BY p.createdAt DESC")
    List<Payment> findByRestaurantAndStatus(
            @Param("restaurantId") Long restaurantId,
            @Param("status") Payment.PaymentStatus status);

    @Query("SELECT p FROM Payment p " +
            "WHERE p.restaurant.id = :restaurantId " +
            "AND p.createdAt >= :since " +
            "ORDER BY p.createdAt DESC")
    List<Payment> findRecentPayments(
            @Param("restaurantId") Long restaurantId,
            @Param("since") LocalDateTime since);

    @Query("SELECT SUM(p.amount) FROM Payment p " +
            "WHERE p.restaurant.id = :restaurantId " +
            "AND p.status = 'COMPLETED' " +
            "AND p.createdAt >= :since")
    BigDecimal calculateRevenue(
            @Param("restaurantId") Long restaurantId,
            @Param("since") LocalDateTime since);
}

