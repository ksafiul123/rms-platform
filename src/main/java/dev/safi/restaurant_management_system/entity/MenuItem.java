package dev.safi.restaurant_management_system.entity;

import dev.safi.restaurant_management_system.enums.ItemType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Menu Item - Individual food/beverage items
 */
@Entity
@Table(name = "menu_items", indexes = {
        @Index(name = "idx_item_restaurant", columnList = "restaurant_id"),
        @Index(name = "idx_item_category", columnList = "category_id"),
        @Index(name = "idx_item_sku", columnList = "sku"),
        @Index(name = "idx_item_active", columnList = "restaurant_id, is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private MenuCategory category;

    @Column(nullable = false, length = 20)
    private String sku;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "discounted_price", precision = 10, scale = 2)
    private BigDecimal discountedPrice;

    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "preparation_time_minutes")
    private Integer preparationTimeMinutes;

    @Column(length = 50)
    @Enumerated(EnumType.STRING)
    private ItemType itemType;

    @Column(name = "is_vegetarian")
    private Boolean isVegetarian = false;

    @Column(name = "is_vegan")
    private Boolean isVegan = false;

    @Column(name = "is_gluten_free")
    private Boolean isGlutenFree = false;

    @Column(name = "is_spicy")
    private Boolean isSpicy = false;

    @Column(name = "spice_level")
    private Integer spiceLevel;

    @Column(name = "calories")
    private Integer calories;

    @Column(name = "allergen_info", length = 500)
    private String allergenInfo;

    @Column(name = "is_available")
    private Boolean isAvailable = true;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @Column(name = "is_best_seller")
    private Boolean isBestSeller = false;

    @Column(name = "available_from")
    private LocalTime availableFrom;

    @Column(name = "available_to")
    private LocalTime availableTo;

    @Column(name = "available_for_dine_in")
    private Boolean availableForDineIn = true;

    @Column(name = "available_for_takeaway")
    private Boolean availableForTakeaway = true;

    @Column(name = "available_for_delivery")
    private Boolean availableForDelivery = true;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @Column(name = "low_stock_threshold")
    private Integer lowStockThreshold;

    @Column(name = "display_order")
    private Integer displayOrder;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
