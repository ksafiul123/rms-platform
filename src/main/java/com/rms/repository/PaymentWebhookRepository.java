package com.rms.repository;

import com.rms.entity.Payment;
import com.rms.entity.PaymentWebhook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentWebhookRepository extends JpaRepository<PaymentWebhook, Long> {

    Optional<PaymentWebhook> findByEventId(String eventId);

    @Query("SELECT w FROM PaymentWebhook w " +
            "WHERE w.isProcessed = false " +
            "AND w.retryCount < 3 " +
            "ORDER BY w.receivedAt ASC")
    List<PaymentWebhook> findUnprocessedWebhooks();

    @Query("SELECT w FROM PaymentWebhook w " +
            "WHERE w.provider = :provider " +
            "AND w.receivedAt >= :since " +
            "ORDER BY w.receivedAt DESC")
    List<PaymentWebhook> findRecentWebhooks(
            @Param("provider") Payment.PaymentProvider provider,
            @Param("since") LocalDateTime since);
}
