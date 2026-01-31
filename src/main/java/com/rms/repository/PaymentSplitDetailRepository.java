package com.rms.repository;

import com.rms.entity.PaymentSplitDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentSplitDetailRepository extends JpaRepository<PaymentSplitDetail, Long> {

    List<PaymentSplitDetail> findByParentPaymentId(Long parentPaymentId);
}
