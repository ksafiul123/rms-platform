package com.rms.entity;

//package com.rms.entity;

import com.rms.enums.ActionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Permission Entity - Granular access control
 */
@Entity
@jakarta.persistence.Table(name = "permissions", indexes = {
        @Index(name = "idx_permission_name", columnList = "name"),
        @Index(name = "idx_permission_resource", columnList = "resource")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 100)
    private String displayName;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, length = 50)
    private String resource;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ActionType action;

    @Column(length = 50)
    private String category;

    @Column(name = "is_system")
    private Boolean isSystem = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}




