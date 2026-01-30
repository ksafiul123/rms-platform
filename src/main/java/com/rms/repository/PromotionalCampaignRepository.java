package com.rms.repository;

import com.rms.entity.PromotionalCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PromotionalCampaignRepository extends JpaRepository<PromotionalCampaign, Long> {

    List<PromotionalCampaign> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);

    List<PromotionalCampaign> findByStatus(PromotionalCampaign.CampaignStatus status);

    @Query("SELECT c FROM PromotionalCampaign c " +
            "WHERE c.status = 'SCHEDULED' " +
            "AND c.scheduledAt <= :now")
    List<PromotionalCampaign> findDueForSending(@Param("now") LocalDateTime now);

    @Query("SELECT c FROM PromotionalCampaign c " +
            "WHERE c.restaurant.id = :restaurantId " +
            "AND c.status = :status " +
            "ORDER BY c.createdAt DESC")
    List<PromotionalCampaign> findByRestaurantAndStatus(
            @Param("restaurantId") Long restaurantId,
            @Param("status") PromotionalCampaign.CampaignStatus status);
}
