package com.rms.repository;

import com.rms.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    List<PaymentTransaction> findByPaymentIdOrderByTransactionTimestampDesc(Long paymentId);

    List<PaymentTransaction> findByTransactionType(PaymentTransaction.TransactionType type);
}
