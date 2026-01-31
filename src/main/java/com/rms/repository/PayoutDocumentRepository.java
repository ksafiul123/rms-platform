package com.rms.repository;

import com.rms.entity.PayoutDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PayoutDocumentRepository extends JpaRepository<PayoutDocument, Long> {

    List<PayoutDocument> findByPayoutIdOrderByUploadedAtDesc(Long payoutId);

    List<PayoutDocument> findByPayoutIdAndDocumentType(
            Long payoutId, PayoutDocument.DocumentType documentType);
}
