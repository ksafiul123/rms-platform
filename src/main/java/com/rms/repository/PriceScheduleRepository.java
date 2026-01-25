package com.rms.repository;

import com.rms.entity.PriceSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Price Schedule Repository
 */
@Repository
public interface PriceScheduleRepository extends JpaRepository<PriceSchedule, Long> {

    List<PriceSchedule> findByMenuItemId(Long menuItemId);

    @Query("SELECT ps FROM PriceSchedule ps WHERE ps.menuItem.id = :menuItemId AND ps.isActive = true " +
            "AND (:dayOfWeek IS NULL OR ps.dayOfWeek IS NULL OR ps.dayOfWeek = :dayOfWeek) " +
            "AND (:currentTime IS NULL OR ps.startTime IS NULL OR :currentTime BETWEEN ps.startTime AND ps.endTime) " +
            "AND (:currentDate IS NULL OR ps.startDate IS NULL OR :currentDate BETWEEN ps.startDate AND ps.endDate) " +
            "ORDER BY ps.id DESC")
    List<PriceSchedule> findActivePriceSchedule(
            @Param("menuItemId") Long menuItemId,
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("currentTime") java.time.LocalTime currentTime,
            @Param("currentDate") java.time.LocalDateTime currentDate
    );
}
