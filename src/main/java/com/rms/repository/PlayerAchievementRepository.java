package com.rms.repository;

import com.rms.entity.PlayerAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerAchievementRepository extends JpaRepository<PlayerAchievement, Long> {

    List<PlayerAchievement> findByUserIdOrderByEarnedAtDesc(Long userId);

    Optional<PlayerAchievement> findByUserIdAndAchievementType(
            Long userId, PlayerAchievement.AchievementType achievementType);

    Long countByUserId(Long userId);
}
