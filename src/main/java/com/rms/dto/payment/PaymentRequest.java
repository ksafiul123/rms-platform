package com.rms.dto.payment;

import com.rms.entity.Payment;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {

    private BigDecimal amount;
    private String currency;
    private Payment.PaymentMethod paymentMethod;
    private Payment.PaymentProvider paymentProvider;
    private String customerPhone;
    private String customerEmail;
    private String ipAddress;
    private String userAgent;
}
