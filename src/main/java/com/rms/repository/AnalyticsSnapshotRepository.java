package com.rms.repository;

// Analytics & Reporting System - Repository Interfaces
// Part of Restaurant Management System

//package com.rms.repository;

import com.rms.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// ============================================
// 1. AnalyticsSnapshotRepository
// ============================================

@Repository
public interface AnalyticsSnapshotRepository extends JpaRepository<AnalyticsSnapshot, Long> {

    Optional<AnalyticsSnapshot> findByRestaurantIdAndReportTypeAndSnapshotDate(
            Long restaurantId,
            AnalyticsSnapshot.ReportType reportType,
            LocalDate snapshotDate
    );

    List<AnalyticsSnapshot> findByRestaurantIdAndReportTypeAndSnapshotDateBetween(
            Long restaurantId,
            AnalyticsSnapshot.ReportType reportType,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query("DELETE FROM AnalyticsSnapshot a WHERE a.cacheExpiresAt < :now")
    @Modifying
    void deleteExpiredSnapshots(@Param("now") LocalDateTime now);
}
