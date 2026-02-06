package com.rms.entity;

//package com.rms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Menu Category - Organize menu items into categories
 */
@Entity
@jakarta.persistence.Table(name = "menu_categories", indexes = {
        @Index(name = "idx_category_restaurant", columnList = "restaurant_id"),
        @Index(name = "idx_category_parent", columnList = "parent_category_id"),
        @Index(name = "idx_category_active", columnList = "restaurant_id, is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "icon_name", length = 50)
    private String iconName;

    // Support for nested categories (e.g., Beverages > Hot Drinks > Coffee)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private MenuCategory parentCategory;

    @OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL)
    private Set<MenuCategory> subCategories = new HashSet<>();

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @Column(name = "available_from")
    private LocalTime availableFrom;

    @Column(name = "available_to")
    private LocalTime availableTo;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

