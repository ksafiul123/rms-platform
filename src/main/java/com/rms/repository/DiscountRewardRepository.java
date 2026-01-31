package com.rms.repository;

import com.rms.entity.DiscountReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountRewardRepository extends JpaRepository<DiscountReward, Long> {

    Optional<DiscountReward> findByRewardCode(String rewardCode);

    Optional<DiscountReward> findByRewardCodeAndStatus(
            String rewardCode, DiscountReward.RewardStatus status);

    List<DiscountReward> findByUserIdAndStatusOrderByEarnedAtDesc(
            Long userId, DiscountReward.RewardStatus status);

    @Query("SELECT dr FROM DiscountReward dr " +
            "WHERE dr.user.id = :userId " +
            "AND dr.status = 'ACTIVE' " +
            "AND dr.expiresAt > :now " +
            "ORDER BY dr.earnedAt DESC")
    List<DiscountReward> findActiveRewards(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now);

    @Query("UPDATE DiscountReward dr " +
            "SET dr.status = 'EXPIRED' " +
            "WHERE dr.status = 'ACTIVE' " +
            "AND dr.expiresAt <= :now")
    void expireOldRewards(@Param("now") LocalDateTime now);
}
