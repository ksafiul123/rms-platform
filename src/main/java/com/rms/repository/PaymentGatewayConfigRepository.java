package com.rms.repository;

import com.rms.entity.Payment;
import com.rms.entity.PaymentGatewayConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentGatewayConfigRepository extends JpaRepository<PaymentGatewayConfig, Long> {

    Optional<PaymentGatewayConfig> findByRestaurantIdAndProvider(
            Long restaurantId, Payment.PaymentProvider provider);

    List<PaymentGatewayConfig> findByRestaurantIdAndIsEnabledTrue(Long restaurantId);

    List<PaymentGatewayConfig> findByRestaurantId(Long restaurantId);
}
