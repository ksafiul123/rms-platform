package com.rms.entity;

//package com.rms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Stores customer's specific preferences for individual menu items
 * Overrides global preferences for specific items
 */
@Entity
@Table(name = "menu_item_preferences",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"customer_id", "menu_item_id"})
        },
        indexes = {
                @Index(name = "idx_menu_item_pref_customer", columnList = "customer_id"),
                @Index(name = "idx_menu_item_pref_item", columnList = "menu_item_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "menu_item_id", nullable = false)
    private Long menuItemId;

    @Enumerated(EnumType.STRING)
    @Column(name = "spice_level", length = 20)
    private CustomerPreference.SpiceLevel spiceLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "cooking_preference", length = 20)
    private CustomerPreference.CookingPreference cookingPreference;

    @Column(name = "extra_ingredients", length = 500)
    private String extraIngredients; // Comma-separated: "extra cheese, extra sauce"

    @Column(name = "remove_ingredients", length = 500)
    private String removeIngredients; // Comma-separated: "no onions, no cilantro"

    @Column(name = "special_instructions", columnDefinition = "TEXT")
    private String specialInstructions;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
