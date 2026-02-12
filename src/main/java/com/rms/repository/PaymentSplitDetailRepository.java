package com.rms.repository;

import com.rms.entity.PaymentSplitDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentSplitDetailRepository extends JpaRepository<PaymentSplitDetail, Long> {

    @Query("SELECT psd FROM PaymentSplitDetail psd WHERE psd.parentPayment.id = :parentPaymentId")
    List<PaymentSplitDetail> findByParentPaymentId(@Param("parentPaymentId") Long parentPaymentId);
}
