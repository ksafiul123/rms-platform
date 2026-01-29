package com.rms.repository;

import com.rms.entity.DeliveryAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryAssignmentRepository extends JpaRepository<DeliveryAssignment, Long> {

    Optional<DeliveryAssignment> findByOrderId(Long orderId);

    List<DeliveryAssignment> findByDeliveryPartnerId(Long deliveryPartnerId);

    List<DeliveryAssignment> findByStatus(DeliveryAssignment.DeliveryStatus status);

    @Query("SELECT d FROM DeliveryAssignment d " +
            "WHERE d.deliveryPartner.id = :partnerId " +
            "AND d.status IN :statuses " +
            "ORDER BY d.assignedAt DESC")
    List<DeliveryAssignment> findActiveDeliveriesForPartner(
            @Param("partnerId") Long partnerId,
            @Param("statuses") List<DeliveryAssignment.DeliveryStatus> statuses);

    @Query("SELECT d FROM DeliveryAssignment d " +
            "WHERE d.order.restaurantId = :restaurantId " +
            "AND d.status IN :statuses " +
            "ORDER BY d.assignedAt ASC")
    List<DeliveryAssignment> findActiveDeliveriesByRestaurant(
            @Param("restaurantId") Long restaurantId,
            @Param("statuses") List<DeliveryAssignment.DeliveryStatus> statuses);

    @Query("SELECT d FROM DeliveryAssignment d " +
            "WHERE d.deliveryPartner.id = :partnerId " +
            "AND d.assignedAt BETWEEN :start AND :end " +
            "ORDER BY d.assignedAt DESC")
    List<DeliveryAssignment> findDeliveriesByPartnerAndDateRange(
            @Param("partnerId") Long partnerId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(d) FROM DeliveryAssignment d " +
            "WHERE d.deliveryPartner.id = :partnerId " +
            "AND d.status IN ('ASSIGNED', 'ACCEPTED', 'PICKED_UP', 'IN_TRANSIT')")
    long countActiveDeliveriesForPartner(@Param("partnerId") Long partnerId);
}
