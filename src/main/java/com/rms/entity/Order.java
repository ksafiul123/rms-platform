package com.rms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@jakarta.persistence.Table(name = "orders", indexes = {
        @Index(name = "idx_restaurant_id", columnList = "restaurant_id"),
        @Index(name = "idx_customer_id", columnList = "customer_id"),
        @Index(name = "idx_order_status", columnList = "status"),
        @Index(name = "idx_order_type", columnList = "order_type"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", insertable = false, updatable = false)
    private Restaurant restaurant;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    private User customer;

    @Column(name = "order_number", nullable = false, unique = true, length = 20)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 20)
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "table_number", length = 10)
    private String tableNumber; // For dine-in orders

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_session_id")
    private TableSession tableSession; // Link to table session for QR orders

    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress; // For delivery orders

    @Column(name = "delivery_man_id")
    private Long deliveryManId;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "tax_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "delivery_fee", precision = 10, scale = 2)
    private BigDecimal deliveryFee;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "special_instructions", columnDefinition = "TEXT")
    private String specialInstructions;

    @Column(name = "kitchen_notes", columnDefinition = "TEXT")
    private String kitchenNotes;

    @Column(name = "estimated_preparation_time_minutes")
    private Integer estimatedPreparationTimeMinutes;

    @Column(name = "priority", nullable = false)
    private Integer priority = 0;

    @Column(name = "is_rush_order", nullable = false)
    private Boolean isRushOrder = false;

    @Column(name = "estimated_ready_time")
    private LocalDateTime estimatedReadyTime;

    @Column(name = "actual_ready_time")
    private LocalDateTime actualReadyTime;

    @Column(name = "delivery_time")
    private LocalDateTime deliveryTime;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancelled_by")
    private Long cancelledBy;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KitchenOrderItem> kitchenItems = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Helper methods
    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }

    public void addStatusHistory(OrderStatusHistory history) {
        statusHistory.add(history);
        history.setOrder(this);
    }

    public enum OrderType {
        DINE_IN,
        TAKEAWAY,
        DELIVERY
    }

    public enum OrderStatus {
        PENDING,        // Order placed, awaiting confirmation
        CONFIRMED,      // Order confirmed by restaurant
        PREPARING,      // Kitchen is preparing
        READY,          // Order ready for pickup/delivery
        OUT_FOR_DELIVERY, // Delivery man picked up
        DELIVERED,      // Order delivered
        COMPLETED,      // Order delivered/picked up
        CANCELLED       // Order cancelled
    }

    public enum PaymentStatus {
        PENDING,
        PAID,
        REFUNDED
    }

    public Payment getPayment() {
        return payments.isEmpty() ? null : payments.get(0);
    }

    public String getCustomerPhone() {
        if (getPayment() != null && getPayment().getCustomerPhone() != null) {
            return getPayment().getCustomerPhone();
        }
        return customer != null ? customer.getPhoneNumber() : null;
    }
}
