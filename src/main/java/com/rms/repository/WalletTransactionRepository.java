package com.rms.repository;

import com.rms.entity.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    Page<WalletTransaction> findByWalletIdOrderByTransactionTimestampDesc(
            Long walletId, Pageable pageable);

    List<WalletTransaction> findByPaymentId(Long paymentId);

    List<WalletTransaction> findByOrderId(Long orderId);
}
