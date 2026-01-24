package dev.safi.restaurant_management_system.entity;

//package com.rms.entity;

import dev.safi.restaurant_management_system.enums.BusinessType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Restaurant Settings - Store-level configurations
 */
@Entity
@Table(name = "restaurant_settings", indexes = {
        @Index(name = "idx_restaurant_settings_restaurant", columnList = "restaurant_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "restaurant_id", nullable = false, unique = true)
    private Long restaurantId;

    @Column(name = "business_name", length = 200)
    private String businessName;

    @Column(name = "tax_registration_number", length = 50)
    private String taxRegistrationNumber;

    @Column(name = "gst_number", length = 50)
    private String gstNumber;

    @Column(name = "business_type", length = 50)
    @Enumerated(EnumType.STRING)
    private BusinessType businessType;

    @Column(length = 20)
    private String currency = "BDT";

    @Column(length = 50)
    private String timezone = "Asia/Dhaka";

    @Column(length = 10)
    private String language = "en";

    @Column(name = "tax_percentage", precision = 5, scale = 2)
    private BigDecimal taxPercentage = BigDecimal.ZERO;

    @Column(name = "service_charge_percentage", precision = 5, scale = 2)
    private BigDecimal serviceChargePercentage = BigDecimal.ZERO;

    @Column(name = "auto_accept_orders")
    private Boolean autoAcceptOrders = false;

    @Column(name = "allow_online_payments")
    private Boolean allowOnlinePayments = true;

    @Column(name = "allow_cash_payments")
    private Boolean allowCashPayments = true;

    @Column(name = "minimum_order_amount", precision = 10, scale = 2)
    private BigDecimal minimumOrderAmount = BigDecimal.ZERO;

    @Column(name = "delivery_radius_km", precision = 5, scale = 2)
    private BigDecimal deliveryRadiusKm;

    @Column(name = "average_preparation_time_minutes")
    private Integer averagePreparationTimeMinutes = 30;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "banner_url", length = 500)
    private String bannerUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}


