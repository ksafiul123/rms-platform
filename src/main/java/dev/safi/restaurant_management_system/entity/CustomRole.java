package dev.safi.restaurant_management_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Custom Role - Restaurant-specific custom roles
 */
@Entity
@Table(name = "custom_roles", indexes = {
        @Index(name = "idx_custom_role_restaurant", columnList = "restaurant_id"),
        @Index(name = "idx_custom_role_name", columnList = "name, restaurant_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String displayName;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "based_on_role_id")
    private Role basedOnRole;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "custom_role_permissions",
            joinColumns = @JoinColumn(name = "custom_role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_by")
    private Long createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
