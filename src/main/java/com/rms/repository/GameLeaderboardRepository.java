package com.rms.repository;

import com.rms.entity.GameLeaderboard;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface GameLeaderboardRepository extends JpaRepository<GameLeaderboard, Long> {

    Optional<GameLeaderboard> findByUserIdAndRestaurantIdAndPeriodTypeAndPeriodDate(
            Long userId, Long restaurantId,
            GameLeaderboard.PeriodType periodType, LocalDate periodDate);

    List<GameLeaderboard> findByRestaurantIdAndPeriodTypeAndPeriodDateOrderByTotalPointsDesc(
            Long restaurantId, GameLeaderboard.PeriodType periodType, LocalDate periodDate);

    @Query("SELECT gl FROM GameLeaderboard gl " +
            "WHERE gl.restaurant.id = :restaurantId " +
            "AND gl.periodType = :periodType " +
            "AND gl.periodDate = :periodDate " +
            "ORDER BY gl.totalPoints DESC")
    List<GameLeaderboard> findTopByRestaurantIdAndPeriodTypeAndPeriodDate(
            @Param("restaurantId") Long restaurantId,
            @Param("periodType") GameLeaderboard.PeriodType periodType,
            @Param("periodDate") LocalDate periodDate,
            Pageable pageable);
}
