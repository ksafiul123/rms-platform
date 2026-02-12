package com.rms.repository;

import com.rms.entity.KitchenOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KitchenOrderItemRepository extends JpaRepository<KitchenOrderItem, Long> {

    @Query("SELECT koi FROM KitchenOrderItem koi WHERE koi.order.id = :orderId")
    List<KitchenOrderItem> findByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT koi FROM KitchenOrderItem koi WHERE koi.order.id = :orderId AND koi.status = :status")
    List<KitchenOrderItem> findByOrderIdAndStatus(
            @Param("orderId") Long orderId, @Param("status") KitchenOrderItem.ItemStatus status);

    @Query("SELECT koi FROM KitchenOrderItem koi WHERE koi.assignedChef.id = :chefId")
    List<KitchenOrderItem> findByAssignedChefId(@Param("chefId") Long chefId);

    List<KitchenOrderItem> findByStation(String station);

    @Query("SELECT k FROM KitchenOrderItem k " +
            "WHERE k.order.restaurantId = :restaurantId " +
            "AND k.status IN :statuses " +
            "ORDER BY k.priority DESC, k.createdAt ASC")
    List<KitchenOrderItem> findActiveItemsByRestaurant(
            @Param("restaurantId") Long restaurantId,
            @Param("statuses") List<KitchenOrderItem.ItemStatus> statuses);

    @Query("SELECT COUNT(k) FROM KitchenOrderItem k " +
            "WHERE k.order.id = :orderId AND k.status = :status")
    long countByOrderIdAndStatus(
            @Param("orderId") Long orderId,
            @Param("status") KitchenOrderItem.ItemStatus status);

    @Query("SELECT k FROM KitchenOrderItem k " +
            "WHERE k.assignedChef.id = :chefId " +
            "AND k.status = 'IN_PROGRESS' " +
            "ORDER BY k.priority DESC")
    List<KitchenOrderItem> findActiveItemsForChef(@Param("chefId") Long chefId);
}
