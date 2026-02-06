package com.rms.entity;

//package com.rms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Tracks customer's favorite menu items across different restaurants
 */
@Entity
@jakarta.persistence.Table(name = "favorite_menu_items",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"customer_id", "menu_item_id"})
        },
        indexes = {
                @Index(name = "idx_favorite_customer", columnList = "customer_id"),
                @Index(name = "idx_favorite_menu_item", columnList = "menu_item_id"),
                @Index(name = "idx_favorite_restaurant", columnList = "restaurant_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteMenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "menu_item_id", nullable = false)
    private Long menuItemId;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(name = "notes", length = 500)
    private String notes; // Custom preparation notes for this favorite

    @Column(name = "order_count", nullable = false)
    private Integer orderCount = 0; // Track how many times ordered

    @Column(name = "last_ordered_at")
    private LocalDateTime lastOrderedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void incrementOrderCount() {
        this.orderCount++;
        this.lastOrderedAt = LocalDateTime.now();
    }
}
