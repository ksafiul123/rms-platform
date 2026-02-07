package com.rms.entity;

//package com.rms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@jakarta.persistence.Table(name = "table_session_guests",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"session_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_session_guest_session", columnList = "session_id"),
                @Index(name = "idx_session_guest_user", columnList = "user_id"),
                @Index(name = "idx_session_guest_joined", columnList = "joined_at")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableSessionGuest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private TableSession session;

    @Column(name = "user_id")
    private Long userId; // Null for anonymous guests

    @Column(name = "guest_name", length = 100)
    private String guestName; // For anonymous guests

    @Column(name = "is_host", nullable = false)
    private Boolean isHost = false; // First guest who scanned QR

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private GuestStatus status;

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    public enum GuestStatus {
        ACTIVE,     // Currently at table
        LEFT        // Left the table
    }

    public boolean isActive() {
        return status == GuestStatus.ACTIVE;
    }

    public void setSession(TableSession session) {
        this.session = session;
    }
}
