package com.rms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@jakarta.persistence.Table(name = "popular_items", indexes = {
        @Index(name = "idx_popular_item_restaurant_date", columnList = "restaurant_id, analysis_date"),
        @Index(name = "idx_popular_item_menu_item", columnList = "menu_item_id, analysis_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PopularItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @Column(name = "analysis_date", nullable = false)
    private LocalDate analysisDate;

    @Column(name = "total_quantity_sold", nullable = false)
    @Builder.Default
    private Integer totalQuantitySold = 0;

    @Column(name = "total_revenue", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(name = "order_count", nullable = false)
    @Builder.Default
    private Integer orderCount = 0;

    @Column(name = "average_price", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal averagePrice = BigDecimal.ZERO;

    @Column(name = "revenue_share", precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal revenueShare = BigDecimal.ZERO;

//    public LocalDate getAnalysisDate() {
//        return analysisDate;
//    }
//
//    public Integer getTotalQuantitySold() {
//        return totalQuantitySold;
//    }
//
//    public BigDecimal getTotalRevenue() {
//        return totalRevenue;
//    }
//
//    public Integer getOrderCount() {
//        return orderCount;
//    }
//
//    public BigDecimal getAveragePrice() {
//        return averagePrice;
//    }
//
//    public BigDecimal getRevenueShare() {
//        return revenueShare;
//    }
//
//    public MenuItem getMenuItem() {
//        return menuItem;
//    }
}
