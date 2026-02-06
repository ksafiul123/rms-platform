package com.rms.service.payment;

import com.rms.dto.payment.PaymentRequest;

public interface PaymentProvider {

    String charge(PaymentRequest request);
}
