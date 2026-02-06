package com.rms.service.payment;

import com.rms.dto.payment.PaymentRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class DefaultPaymentProvider implements PaymentProvider {

    @Override
    public String charge(PaymentRequest request) {
        String transactionId = "MANUAL-" + UUID.randomUUID();
        log.warn("Using default payment provider. Returning transaction id {}", transactionId);
        return transactionId;
    }
}
