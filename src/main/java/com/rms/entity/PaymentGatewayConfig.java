package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@jakarta.persistence.Table(name = "payment_gateway_configs", indexes = {
        @Index(name = "idx_gateway_restaurant", columnList = "restaurant_id"),
        @Index(name = "idx_gateway_provider", columnList = "provider")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentGatewayConfig extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 30)
    private Payment.PaymentProvider provider;

    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private Boolean isEnabled = false;

    @Column(name = "is_test_mode", nullable = false)
    @Builder.Default
    private Boolean isTestMode = true;

    @Column(name = "api_key_encrypted", length = 500)
    private String apiKeyEncrypted;

    @Column(name = "api_secret_encrypted", length = 500)
    private String apiSecretEncrypted;

    @Column(name = "webhook_secret_encrypted", length = 500)
    private String webhookSecretEncrypted;

    @Column(name = "merchant_id", length = 200)
    private String merchantId;

    @Column(name = "config_json", columnDefinition = "TEXT")
    private String configJson;

    @Column(name = "supported_currencies", length = 100)
    private String supportedCurrencies;

    @Column(name = "transaction_fee_percentage", precision = 5, scale = 2)
    private BigDecimal transactionFeePercentage;

    @Column(name = "transaction_fee_fixed", precision = 10, scale = 2)
    private BigDecimal transactionFeeFixed;

    @Column(name = "min_transaction_amount", precision = 10, scale = 2)
    private BigDecimal minTransactionAmount;

    @Column(name = "max_transaction_amount", precision = 10, scale = 2)
    private BigDecimal maxTransactionAmount;

    @Column(name = "last_tested_at")
    private LocalDateTime lastTestedAt;

    @Column(name = "test_status", length = 20)
    private String testStatus;
}
