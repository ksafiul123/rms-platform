package com.rms.controller.analytics;

import com.rms.dto.auth.ApiResponse;
import com.rms.dto.analytics.*;
import com.rms.security.annotation.RequirePermission;
import com.rms.service.analytics.AnalyticsService;
import com.rms.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // ==================== Dashboard ====================

    @GetMapping("/dashboard")
    @RequirePermission("analytics:view")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getDashboardSummary() {
        Long restaurantId = SecurityUtil.getCurrentRestaurantId();
        DashboardSummaryResponse dashboard = analyticsService.getDashboardSummary(restaurantId);
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    @GetMapping("/performance")
    @RequirePermission("analytics:view")
    public ResponseEntity<ApiResponse<PerformanceMetricsResponse>> getPerformanceMetrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Long restaurantId = SecurityUtil.getCurrentRestaurantId();
        LocalDate targetDate = date != null ? date : LocalDate.now();

        PerformanceMetricsResponse metrics =
                analyticsService.getPerformanceMetrics(restaurantId, targetDate);

        return ResponseEntity.ok(ApiResponse.success(metrics));
    }

    // ==================== Sales Reports ====================

    @GetMapping("/sales/daily")
    @RequirePermission("analytics:view")
    public ResponseEntity<ApiResponse<DailySalesReportResponse>> getDailySalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Long restaurantId = SecurityUtil.getCurrentRestaurantId();
        DailySalesReportResponse report = analyticsService.getDailySalesReport(restaurantId, date);

        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/sales/range")
    @RequirePermission("analytics:view")
    public ResponseEntity<ApiResponse<List<DailySalesReportResponse>>> getSalesReportRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long restaurantId = SecurityUtil.getCurrentRestaurantId();
        List<DailySalesReportResponse> reports =
                analyticsService.getSalesReportRange(restaurantId, startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(reports));
    }

    @GetMapping("/sales/compare")
    @RequirePermission("analytics:view")
    public ResponseEntity<ApiResponse<SalesComparisonResponse>> compareSalesPeriods(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate period1Start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate period1End,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate period2Start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate period2End) {

        Long restaurantId = SecurityUtil.getCurrentRestaurantId();
        SalesComparisonResponse comparison = analyticsService.compareSalesPeriods(
                restaurantId, period1Start, period1End, period2Start, period2End);

        return ResponseEntity.ok(ApiResponse.success(comparison));
    }

    // ==================== Popular Items ====================

    @GetMapping("/items/popular")
    @RequirePermission("analytics:view")
    public ResponseEntity<ApiResponse<List<PopularItemResponse>>> getPopularItems(
            @RequestParam(defaultValue = "MONTH") String period,
            @RequestParam(defaultValue = "10") int limit) {

        Long restaurantId = SecurityUtil.getCurrentRestaurantId();
        List<PopularItemResponse> items =
                analyticsService.getPopularItems(restaurantId, period, limit);

        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @GetMapping("/items/analysis")
    @RequirePermission("analytics:view")
    public ResponseEntity<ApiResponse<PopularItemsAnalysisResponse>> analyzePopularItems(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long restaurantId = SecurityUtil.getCurrentRestaurantId();
        PopularItemsAnalysisResponse analysis =
                analyticsService.analyzePopularItems(restaurantId, startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(analysis));
    }

    @GetMapping("/items/{menuItemId}/trend")
    @RequirePermission("analytics:view")
    public ResponseEntity<ApiResponse<List<ItemPerformanceTrendResponse>>> getItemPerformanceTrend(
            @PathVariable Long menuItemId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<ItemPerformanceTrendResponse> trend =
                analyticsService.getItemPerformanceTrend(menuItemId, startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(trend));
    }

    // ==================== Customer Behavior ====================

    @GetMapping("/customers/behavior")
    @RequirePermission("analytics:view")
    public ResponseEntity<ApiResponse<CustomerBehaviorResponse>> getCustomerBehavior(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Long restaurantId = SecurityUtil.getCurrentRestaurantId();
        LocalDate targetDate = date != null ? date : LocalDate.now();

        CustomerBehaviorResponse behavior =
                analyticsService.getCustomerBehavior(restaurantId, targetDate);

        return ResponseEntity.ok(ApiResponse.success(behavior));
    }

    @GetMapping("/customers/segmentation")
    @RequirePermission("analytics:view")
    public ResponseEntity<ApiResponse<CustomerSegmentationResponse>> getCustomerSegmentation() {

        Long restaurantId = SecurityUtil.getCurrentRestaurantId();
        CustomerSegmentationResponse segmentation =
                analyticsService.getCustomerSegmentation(restaurantId);

        return ResponseEntity.ok(ApiResponse.success(segmentation));
    }

    @GetMapping("/customers/{customerId}/lifetime-value")
    @RequirePermission("analytics:view")
    public ResponseEntity<ApiResponse<CustomerLifetimeValueResponse>> getCustomerLifetimeValue(
            @PathVariable Long customerId) {

        CustomerLifetimeValueResponse clv =
                analyticsService.getCustomerLifetimeValue(customerId);

        return ResponseEntity.ok(ApiResponse.success(clv));
    }

    // ==================== Inventory Usage ====================

    @GetMapping("/inventory/usage")
    @RequirePermission("analytics:view")
    public ResponseEntity<ApiResponse<List<InventoryUsageResponse>>> getInventoryUsage(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long restaurantId = SecurityUtil.getCurrentRestaurantId();
        List<InventoryUsageResponse> usage =
                analyticsService.getInventoryUsage(restaurantId, startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(usage));
    }

    @GetMapping("/inventory/efficiency")
    @RequirePermission("analytics:view")
    public ResponseEntity<ApiResponse<InventoryEfficiencyResponse>> getInventoryEfficiency(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Long restaurantId = SecurityUtil.getCurrentRestaurantId();
        LocalDate targetDate = date != null ? date : LocalDate.now();

        InventoryEfficiencyResponse efficiency =
                analyticsService.analyzeInventoryEfficiency(restaurantId, targetDate);

        return ResponseEntity.ok(ApiResponse.success(efficiency));
    }

    @GetMapping("/inventory/slow-moving")
    @RequirePermission("analytics:view")
    public ResponseEntity<ApiResponse<List<SlowMovingItemResponse>>> getSlowMovingItems(
            @RequestParam(defaultValue = "90") int daysThreshold) {

        Long restaurantId = SecurityUtil.getCurrentRestaurantId();
        List<SlowMovingItemResponse> items =
                analyticsService.getSlowMovingItems(restaurantId, daysThreshold);

        return ResponseEntity.ok(ApiResponse.success(items));
    }

    // ==================== Revenue Analytics ====================

    @GetMapping("/revenue")
    @RequirePermission("analytics:view")
    public ResponseEntity<ApiResponse<RevenueAnalyticsResponse>> getRevenueAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long restaurantId = SecurityUtil.getCurrentRestaurantId();
        RevenueAnalyticsResponse analytics =
                analyticsService.getRevenueAnalytics(restaurantId, startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(analytics));
    }

    @GetMapping("/revenue/forecast")
    @RequirePermission("analytics:view")
    public ResponseEntity<ApiResponse<RevenueForecastResponse>> forecastRevenue(
            @RequestParam(defaultValue = "30") int forecastDays) {

        Long restaurantId = SecurityUtil.getCurrentRestaurantId();
        RevenueForecastResponse forecast =
                analyticsService.forecastRevenue(restaurantId, forecastDays);

        return ResponseEntity.ok(ApiResponse.success(forecast));
    }

    // ==================== Hourly Analytics ====================

    @GetMapping("/hourly")
    @RequirePermission("analytics:view")
    public ResponseEntity<ApiResponse<HourlyAnalyticsResponse>> getHourlyAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Long restaurantId = SecurityUtil.getCurrentRestaurantId();
        HourlyAnalyticsResponse analytics =
                analyticsService.getHourlyAnalytics(restaurantId, date);

        return ResponseEntity.ok(ApiResponse.success(analytics));
    }

    // ==================== Export ====================

    @PostMapping("/export")
    @RequirePermission("analytics:export")
    public ResponseEntity<ApiResponse<ReportExportResponse>> exportReport(
            @RequestBody ReportExportRequest request) {

        Long restaurantId = SecurityUtil.getCurrentRestaurantId();
        ReportExportResponse export =
                analyticsService.exportReport(restaurantId, request);

        return ResponseEntity.ok(ApiResponse.success(export, "Report export initiated"));
    }

    @GetMapping("/export/{reportId}/status")
    @RequirePermission("analytics:export")
    public ResponseEntity<ApiResponse<ReportExportResponse>> getExportStatus(
            @PathVariable String reportId) {

        ReportExportResponse status = analyticsService.getExportStatus(reportId);
        return ResponseEntity.ok(ApiResponse.success(status));
    }
}
