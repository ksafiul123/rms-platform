package com.rms.service.payment;

import com.rms.dto.payment.PaymentRequest;
import com.rms.entity.Order;
import com.rms.entity.Payment;
import com.rms.entity.Restaurant;
import com.rms.entity.User;
import com.rms.exception.ResourceNotFoundException;
import com.rms.repository.OrderRepository;
import com.rms.repository.PaymentRepository;
import com.rms.repository.RestaurantRepository;
import com.rms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(isolation = Isolation.READ_COMMITTED, timeout = 30)
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final PaymentProvider paymentProvider;

    public Payment processPayment(Long orderId, PaymentRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        String transactionId = paymentProvider.charge(request);

        Payment payment = createPayment(order, transactionId, request);
        paymentRepository.save(payment);

        order.setPaymentStatus(Order.PaymentStatus.PAID);
        orderRepository.save(order);

        return payment;
    }

    private Payment createPayment(Order order, String transactionId, PaymentRequest request) {
        User customer = userRepository.findById(order.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        Restaurant restaurant = restaurantRepository.findById(order.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        BigDecimal amount = request.getAmount() != null ? request.getAmount() : order.getTotalAmount();
        String currency = request.getCurrency() != null ? request.getCurrency() : "BDT";

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setCustomer(customer);
        payment.setRestaurant(restaurant);
        payment.setPaymentReference(generatePaymentReference());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentProvider(request.getPaymentProvider());
        payment.setAmount(amount);
        payment.setCurrency(currency);
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setProviderTransactionId(transactionId);
        payment.setInitiatedAt(LocalDateTime.now());
        payment.setCompletedAt(LocalDateTime.now());
        payment.setCustomerPhone(request.getCustomerPhone());
        payment.setCustomerEmail(request.getCustomerEmail());
        payment.setIpAddress(request.getIpAddress());
        payment.setUserAgent(request.getUserAgent());
        return payment;
    }

    private String generatePaymentReference() {
        return "PAY-" + UUID.randomUUID();
    }
}
