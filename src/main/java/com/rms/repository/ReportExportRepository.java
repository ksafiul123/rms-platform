package com.rms.repository;

import com.rms.entity.AnalyticsSnapshot;
import com.rms.entity.ReportExport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReportExportRepository extends JpaRepository<ReportExport, Long> {

    List<ReportExport> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);

    List<ReportExport> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<ReportExport> findByRestaurantIdAndStatusOrderByCreatedAtDesc(
            Long restaurantId,
            ReportExport.ExportStatus status
    );

    @Query("SELECT re FROM ReportExport re " +
            "WHERE re.restaurant.id = :restaurantId " +
            "AND re.reportType = :reportType " +
            "ORDER BY re.createdAt DESC")
    Page<ReportExport> findByRestaurantIdAndReportType(
            @Param("restaurantId") Long restaurantId,
            @Param("reportType") AnalyticsSnapshot.ReportType reportType,
            Pageable pageable
    );

    @Query("DELETE FROM ReportExport re WHERE re.expiresAt < :now")
    @Modifying
    void deleteExpiredExports(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(re) FROM ReportExport re " +
            "WHERE re.restaurant.id = :restaurantId " +
            "AND re.status = 'COMPLETED' " +
            "AND re.createdAt >= :since")
    Long countCompletedExportsSince(
            @Param("restaurantId") Long restaurantId,
            @Param("since") LocalDateTime since
    );
}
