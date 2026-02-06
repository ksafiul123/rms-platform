package com.rms.entity;

//package com.rms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores customer's global food preferences shared across all restaurants
 * Visible to chefs to prepare food according to customer preferences
 */
@Entity
@jakarta.persistence.Table(name = "customer_preferences",
        indexes = {
                @Index(name = "idx_customer_pref_customer", columnList = "customer_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false, unique = true)
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "spice_level", length = 20)
    private SpiceLevel spiceLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "sweetness_level", length = 20)
    private SweetnessLevel sweetnessLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "salt_level", length = 20)
    private SaltLevel saltLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "cooking_preference", length = 20)
    private CookingPreference cookingPreference; // Well-done, medium, rare, etc.

    @Enumerated(EnumType.STRING)
    @Column(name = "temperature_preference", length = 20)
    private TemperaturePreference temperaturePreference;

    @Column(name = "is_vegetarian", nullable = false)
    private Boolean isVegetarian = false;

    @Column(name = "is_vegan", nullable = false)
    private Boolean isVegan = false;

    @Column(name = "is_gluten_free", nullable = false)
    private Boolean isGlutenFree = false;

    @Column(name = "is_dairy_free", nullable = false)
    private Boolean isDairyFree = false;

    @Column(name = "is_nut_free", nullable = false)
    private Boolean isNutFree = false;

    @ElementCollection
    @CollectionTable(
            name = "customer_allergies",
            joinColumns = @JoinColumn(name = "customer_preference_id")
    )
    @Column(name = "allergy", length = 100)
    private List<String> allergies = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "customer_dislikes",
            joinColumns = @JoinColumn(name = "customer_preference_id")
    )
    @Column(name = "dislike", length = 100)
    private List<String> dislikes = new ArrayList<>();

    @Column(name = "special_instructions", columnDefinition = "TEXT")
    private String specialInstructions;

    @Column(name = "portion_preference", length = 20)
    private String portionPreference; // Regular, Large, Small

    @Column(name = "visible_to_chefs", nullable = false)
    private Boolean visibleToChefs = true; // Control privacy

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum SpiceLevel {
        NONE,           // No spice
        MILD,           // Little spice
        MEDIUM,         // Moderate spice
        HOT,            // Very spicy
        EXTRA_HOT       // Maximum spice
    }

    public enum SweetnessLevel {
        NO_SUGAR,       // Sugar-free
        LOW,            // Less sweet
        MEDIUM,         // Normal sweetness
        HIGH            // Extra sweet
    }

    public enum SaltLevel {
        NO_SALT,        // Salt-free
        LOW,            // Less salt
        MEDIUM,         // Normal salt
        HIGH            // Extra salt
    }

    public enum CookingPreference {
        RARE,           // For meats
        MEDIUM_RARE,
        MEDIUM,
        MEDIUM_WELL,
        WELL_DONE,
        CRISPY,         // For fried items
        SOFT,           // For breads/pastries
        AL_DENTE        // For pasta
    }

    public enum TemperaturePreference {
        COLD,           // Prefer cold
        ROOM_TEMP,      // Room temperature
        HOT,            // Prefer hot
        EXTRA_HOT       // Very hot
    }

    // Helper methods
    public boolean hasDietaryRestrictions() {
        return isVegetarian || isVegan || isGlutenFree || isDairyFree || isNutFree;
    }

    public boolean hasAllergies() {
        return allergies != null && !allergies.isEmpty();
    }
}