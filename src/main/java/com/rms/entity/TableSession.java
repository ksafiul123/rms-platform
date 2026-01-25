package com.rms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "table_sessions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"session_code"})
        },
        indexes = {
                @Index(name = "idx_table_session_table", columnList = "table_id"),
                @Index(name = "idx_table_session_restaurant", columnList = "restaurant_id"),
                @Index(name = "idx_table_session_status", columnList = "status"),
                @Index(name = "idx_table_session_code", columnList = "session_code"),
                @Index(name = "idx_table_session_started", columnList = "started_at")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private Table table;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(name = "session_code", nullable = false, unique = true, length = 50)
    private String sessionCode; // Unique session identifier for joining

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SessionStatus status;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "guest_count", nullable = false)
    private Integer guestCount = 0;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TableSessionGuest> guests = new ArrayList<>();

    @OneToMany(mappedBy = "tableSession", cascade = CascadeType.ALL)
    private List<Order> orders = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum SessionStatus {
        ACTIVE,         // Session is ongoing
        COMPLETED,      // Session ended, bill paid
        CANCELLED       // Session cancelled
    }

    // Helper methods
    public void addGuest(TableSessionGuest guest) {
        guests.add(guest);
        guest.setSession(this);
        this.guestCount = guests.size();
    }

    public void removeGuest(TableSessionGuest guest) {
        guests.remove(guest);
        guest.setSession(null);
        this.guestCount = guests.size();
    }

    public void addOrder(Order order) {
        orders.add(order);
        order.setTableSession(this);
        recalculateTotalAmount();
    }

    public void recalculateTotalAmount() {
        this.totalAmount = orders.stream()
                .filter(order -> order.getStatus() != Order.OrderStatus.CANCELLED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isActive() {
        return status == SessionStatus.ACTIVE;
    }
}