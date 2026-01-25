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

@Entity
@Table(name = "tables",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"restaurant_id", "table_number"}),
                @UniqueConstraint(columnNames = {"qr_code"})
        },
        indexes = {
                @Index(name = "idx_table_restaurant", columnList = "restaurant_id"),
                @Index(name = "idx_table_qr_code", columnList = "qr_code"),
                @Index(name = "idx_table_status", columnList = "status")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Table {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "table_number", nullable = false, length = 20)
    private String tableNumber;

    @Column(name = "qr_code", nullable = false, unique = true, length = 100)
    private String qrCode; // Unique QR code identifier

    @Column(name = "qr_code_image_url", length = 500)
    private String qrCodeImageUrl; // URL to QR code image

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "floor", length = 50)
    private String floor;

    @Column(name = "section", length = 50)
    private String section;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TableStatus status;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "description", length = 500)
    private String description;

    @OneToMany(mappedBy = "table", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TableSession> sessions = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum TableStatus {
        AVAILABLE,      // Table is free
        OCCUPIED,       // Table has active session
        RESERVED,       // Table is reserved
        CLEANING,       // Table is being cleaned
        MAINTENANCE     // Table is under maintenance
    }

    // Helper methods
    public void addSession(TableSession session) {
        sessions.add(session);
        session.setTable(this);
    }

    public boolean hasActiveSession() {
        return sessions.stream()
                .anyMatch(session -> session.getStatus() == TableSession.SessionStatus.ACTIVE);
    }

    public TableSession getActiveSession() {
        return sessions.stream()
                .filter(session -> session.getStatus() == TableSession.SessionStatus.ACTIVE)
                .findFirst()
                .orElse(null);
    }
}
