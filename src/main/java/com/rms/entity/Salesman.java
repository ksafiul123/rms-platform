package com.rms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Salesman - Track sales team who onboard restaurants
 */
@Entity
@jakarta.persistence.Table(name = "salesmen", indexes = {
        @Index(name = "idx_salesman_user", columnList = "user_id"),
        @Index(name = "idx_salesman_code", columnList = "salesman_code")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Salesman {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "salesman_code", unique = true, nullable = false, length = 20)
    private String salesmanCode;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(length = 500)
    private String territory;

    @Column(name = "commission_percentage", precision = 5, scale = 2)
    private BigDecimal commissionPercentage = BigDecimal.ZERO;

    @Column(name = "total_onboarded")
    private Integer totalOnboarded = 0;

    @Column(name = "total_active")
    private Integer totalActive = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "joined_date")
    private LocalDateTime joinedDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
