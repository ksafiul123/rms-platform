package com.rms.entity;

//package com.rms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@jakarta.persistence.Table(name = "order_item_modifiers", indexes = {
        @Index(name = "idx_order_item_id", columnList = "order_item_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemModifier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @Column(name = "modifier_id", nullable = false)
    private Long modifierId;

    @Column(name = "modifier_name", nullable = false, length = 200)
    private String modifierName; // Snapshot at order time

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price; // Price at order time

//    public void setOrderItem(OrderItem orderItem) {
//        this.orderItem = orderItem;
//    }
//
//    public BigDecimal getPrice() {
//        return price;
//    }
}
