package com.rms.repository;

import com.rms.entity.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    @Query("SELECT wt FROM WalletTransaction wt WHERE wt.wallet.id = :walletId ORDER BY wt.transactionTimestamp DESC")
    Page<WalletTransaction> findByWalletIdOrderByTransactionTimestampDesc(
            @Param("walletId") Long walletId, Pageable pageable);

    @Query("SELECT wt FROM WalletTransaction wt WHERE wt.payment.id = :paymentId")
    List<WalletTransaction> findByPaymentId(@Param("paymentId") Long paymentId);

    @Query("SELECT wt FROM WalletTransaction wt WHERE wt.order.id = :orderId")
    List<WalletTransaction> findByOrderId(@Param("orderId") Long orderId);
}
