// OrderStatusHistory.java
package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@jakarta.persistence.Table(name = "order_status_history", indexes = {
        @Index(name = "idx_status_history_order", columnList = "order_id"),
        @Index(name = "idx_status_history_timestamp", columnList = "timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private Order.OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 50)
    private Order.OrderStatus previousStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "estimated_time_minutes")
    private Integer estimatedTimeMinutes;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "device_info")
    private String deviceInfo;

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    public Order.OrderStatus getFromStatus() {
        return previousStatus;
    }

    public void setFromStatus(Order.OrderStatus status) {
        this.previousStatus = status;
    }

    public Order.OrderStatus getToStatus() {
        return status;
    }

    public void setToStatus(Order.OrderStatus status) {
        this.status = status;
    }

    public void setChangedBy(Long userId) {
        if (userId == null) {
            this.updatedBy = null;
            return;
        }
        User user = new User();
        user.setId(userId);
        this.updatedBy = user;
    }
}
