package com.rms.repository;

import com.rms.entity.AnalyticsSnapshot;
import com.rms.entity.ScheduledReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduledReportRepository extends JpaRepository<ScheduledReport, Long> {

    List<ScheduledReport> findByRestaurantIdAndIsActiveTrue(Long restaurantId);

    @Query("SELECT sr FROM ScheduledReport sr " +
            "WHERE sr.isActive = true " +
            "AND sr.nextRunAt <= :now")
    List<ScheduledReport> findDueReports(@Param("now") LocalDateTime now);

    List<ScheduledReport> findByRestaurantIdAndReportType(
            Long restaurantId,
            AnalyticsSnapshot.ReportType reportType
    );

    @Query("SELECT sr FROM ScheduledReport sr " +
            "WHERE sr.restaurant.id = :restaurantId " +
            "AND sr.frequency = :frequency " +
            "AND sr.isActive = true")
    List<ScheduledReport> findByRestaurantIdAndFrequency(
            @Param("restaurantId") Long restaurantId,
            @Param("frequency") ScheduledReport.Frequency frequency
    );
}
