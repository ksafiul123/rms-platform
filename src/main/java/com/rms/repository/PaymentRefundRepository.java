package com.rms.repository;

import com.rms.entity.PaymentRefund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRefundRepository extends JpaRepository<PaymentRefund, Long> {

    Optional<PaymentRefund> findByRefundReference(String refundReference);

    List<PaymentRefund> findByPaymentIdOrderByInitiatedAtDesc(Long paymentId);

    @Query("SELECT r FROM PaymentRefund r " +
            "WHERE r.payment.order.id = :orderId " +
            "ORDER BY r.initiatedAt DESC")
    List<PaymentRefund> findByOrderId(@Param("orderId") Long orderId);

    List<PaymentRefund> findByStatus(PaymentRefund.RefundStatus status);
}
