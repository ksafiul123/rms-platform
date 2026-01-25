package dev.safi.restaurant_management_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Item Ingredient - Recipe ingredients for menu items
 */
@Entity
@Table(name = "item_ingredients", indexes = {
        @Index(name = "idx_item_ingredient_item", columnList = "menu_item_id"),
        @Index(name = "idx_item_ingredient_ingredient", columnList = "ingredient_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal quantity;

    @Column(length = 50)
    private String unit;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
