package com.rms.entity;

import com.rms.enums.OnboardingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Restaurant Onboarding - Track onboarding process
 */
@Entity
@jakarta.persistence.Table(name = "restaurant_onboarding", indexes = {
        @Index(name = "idx_onboarding_restaurant", columnList = "restaurant_id"),
        @Index(name = "idx_onboarding_salesman", columnList = "salesman_id"),
        @Index(name = "idx_onboarding_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantOnboarding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(name = "salesman_id")
    private Long salesmanId;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private OnboardingStatus status;

    @Column(name = "step_completed")
    private Integer stepCompleted = 0;

    @Column(name = "total_steps")
    private Integer totalSteps = 5;

    @Column(name = "completed_steps", length = 500)
    private String completedSteps;

    @Column(name = "onboarded_at")
    private LocalDateTime onboardedAt;

    @Column(length = 1000)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
