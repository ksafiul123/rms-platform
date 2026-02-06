package com.rms.entity;

import com.rms.enums.OverrideType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Permission Override - User-specific permission overrides
 */
@Entity
@jakarta.persistence.Table(name = "permission_overrides", indexes = {
        @Index(name = "idx_permission_override_user", columnList = "user_id"),
        @Index(name = "idx_permission_override_restaurant", columnList = "restaurant_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "restaurant_id")
    private Long restaurantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OverrideType overrideType;

    @Column(name = "granted_by")
    private Long grantedBy;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(length = 500)
    private String reason;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
