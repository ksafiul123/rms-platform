package com.rms.entity;

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
 * Ingredient - For inventory tracking
 */
@Entity
@jakarta.persistence.Table(name = "ingredients", indexes = {
        @Index(name = "idx_ingredient_restaurant", columnList = "restaurant_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String sku;

    @Column(length = 50)
    private String unit;

    @Column(name = "current_stock", precision = 10, scale = 2)
    private BigDecimal currentStock;

    @Column(name = "min_stock", precision = 10, scale = 2)
    private BigDecimal minStock;

    @Column(name = "cost_per_unit", precision = 10, scale = 2)
    private BigDecimal costPerUnit;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
