package com.rms.service.analytics;

import com.rms.dto.analytics.*;
import com.rms.entity.*;
import com.rms.exception.ResourceNotFoundException;
import com.rms.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AnalyticsService {

    private final SalesReportRepository salesReportRepository;
    private final PopularItemRepository popularItemRepository;
    private final CustomerBehaviorRepository customerBehaviorRepository;
    private final InventoryUsageRepository inventoryUsageRepository;
    private final RevenueAnalyticsRepository revenueAnalyticsRepository;
    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final Map<String, ReportExportResponse> exportReports = new ConcurrentHashMap<>();

    // ==================== Sales Reports ====================

    @Cacheable(value = "dailySalesReport", key = "#restaurantId + '_' + #date")
    public DailySalesReportResponse getDailySalesReport(Long restaurantId, LocalDate date) {

        SalesReport report = salesReportRepository
                .findByRestaurantIdAndReportDate(restaurantId, date)
                .orElse(generateDailySalesReport(restaurantId, date));

        return DailySalesReportResponse.builder()
                .reportDate(report.getReportDate())
                .totalOrders(report.getTotalOrders())
                .totalRevenue(report.getTotalRevenue())
                .totalCost(report.getTotalCost())
                .netProfit(report.getNetProfit())
                .averageOrderValue(report.getAverageOrderValue())
                .totalDiscounts(report.getTotalDiscounts())
                .totalTaxes(report.getTotalTaxes())
                .dineInOrders(report.getDineInOrders())
                .dineInRevenue(report.getDineInRevenue())
                .takeawayOrders(report.getTakeawayOrders())
                .takeawayRevenue(report.getTakeawayRevenue())
                .deliveryOrders(report.getDeliveryOrders())
                .deliveryRevenue(report.getDeliveryRevenue())
                .onlinePayments(report.getOnlinePayments())
                .cashPayments(report.getCashPayments())
                .walletPayments(report.getWalletPayments())
                .uniqueCustomers(report.getUniqueCustomers())
                .newCustomers(report.getNewCustomers())
                .returningCustomers(report.getReturningCustomers())
                .cancelledOrders(report.getCancelledOrders())
                .refundedAmount(report.getRefundedAmount())
                .build();
    }

    // ==================== Dashboard ====================

    public DashboardSummaryResponse getDashboardSummary(Long restaurantId) {
        LocalDate today = LocalDate.now();
        SalesReport report = salesReportRepository
                .findByRestaurantIdAndReportDateAndPeriodType(
                        restaurantId, today, SalesReport.PeriodType.DAILY)
                .orElse(null);

        BigDecimal todayRevenue = report != null ? safe(report.getTotalRevenue()) : BigDecimal.ZERO;
        Integer todayOrders = report != null ? safe(report.getTotalOrders()) : 0;
        BigDecimal avgOrderValue = report != null ? safe(report.getAverageOrderValue()) : BigDecimal.ZERO;
        BigDecimal todayProfit = report != null ? safe(report.getNetRevenue()) : BigDecimal.ZERO;

        BigDecimal revenueYesterday = safe(salesReportRepository.sumRevenueByRestaurantAndPeriod(
                restaurantId, today.minusDays(1), today.minusDays(1)));
        BigDecimal revenueLastWeek = safe(salesReportRepository.sumRevenueByRestaurantAndPeriod(
                restaurantId, today.minusWeeks(1), today.minusDays(1)));
        BigDecimal revenueLastMonth = safe(salesReportRepository.sumRevenueByRestaurantAndPeriod(
                restaurantId, today.minusMonths(1), today.minusDays(1)));

        List<Order> activeOrders = orderRepository.findByRestaurantIdAndStatusIn(
                restaurantId,
                List.of(Order.OrderStatus.CONFIRMED, Order.OrderStatus.PREPARING, Order.OrderStatus.READY));

        int pendingOrders = safe(orderRepository
                .countByRestaurantIdAndStatus(restaurantId, Order.OrderStatus.PENDING))
                .intValue();

        int completedToday = safe(orderRepository
                .countByRestaurantIdAndStatus(restaurantId, Order.OrderStatus.COMPLETED))
                .intValue();

        List<PopularItemResponse> topItems = getPopularItems(restaurantId, "MONTH", 5);

        List<InventoryItem> inventoryItems =
                inventoryItemRepository.findByRestaurantIdAndIsActive(restaurantId, true);

        int lowStockAlerts = (int) inventoryItems.stream()
                .filter(item -> item.getMinimumQuantity() != null
                        && item.getCurrentQuantity().compareTo(item.getMinimumQuantity()) <= 0)
                .count();
        int outOfStockItems = (int) inventoryItems.stream()
                .filter(item -> item.getCurrentQuantity().compareTo(BigDecimal.ZERO) == 0)
                .count();

        return DashboardSummaryResponse.builder()
                .date(today)
                .todayRevenue(todayRevenue)
                .todayOrders(todayOrders)
                .todayProfit(todayProfit)
                .avgOrderValue(avgOrderValue)
                .revenueVsYesterday(todayRevenue.subtract(revenueYesterday))
                .revenueVsLastWeek(todayRevenue.subtract(revenueLastWeek))
                .revenueVsLastMonth(todayRevenue.subtract(revenueLastMonth))
                .activeOrders(activeOrders.size())
                .pendingOrders(pendingOrders)
                .completedToday(completedToday)
                .topItems(topItems)
                .topCustomers(Collections.emptyList())
                .lowStockAlerts(lowStockAlerts)
                .outOfStockItems(outOfStockItems)
                .overdueOrders(0)
                .build();
    }

    // ==================== Performance Metrics ====================

    public PerformanceMetricsResponse getPerformanceMetrics(Long restaurantId, LocalDate date) {
        SalesReport report = salesReportRepository
                .findByRestaurantIdAndReportDateAndPeriodType(
                        restaurantId, date, SalesReport.PeriodType.DAILY)
                .orElse(null);

        BigDecimal totalRevenue = report != null ? safe(report.getTotalRevenue()) : BigDecimal.ZERO;
        BigDecimal averageOrderValue = report != null ? safe(report.getAverageOrderValue()) : BigDecimal.ZERO;
        Integer totalOrders = report != null ? safe(report.getTotalOrders()) : 0;

        BigDecimal grossProfit = report != null ? safe(report.getNetRevenue()) : BigDecimal.ZERO;
        BigDecimal netProfit = grossProfit;
        BigDecimal profitMargin = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                ? netProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;

        return PerformanceMetricsResponse.builder()
                .date(date)
                .totalOrders(totalOrders)
                .orderFulfillmentRate(BigDecimal.ZERO)
                .avgPreparationTime(BigDecimal.ZERO)
                .avgDeliveryTime(BigDecimal.ZERO)
                .onTimeDeliveryRate(BigDecimal.ZERO)
                .totalCustomers(report != null ? safe(report.getUniqueCustomers()) : 0)
                .customerSatisfactionScore(BigDecimal.ZERO)
                .customerRetentionRate(BigDecimal.ZERO)
                .repeatOrderRate(BigDecimal.ZERO)
                .totalRevenue(totalRevenue)
                .grossProfit(grossProfit)
                .netProfit(netProfit)
                .profitMargin(profitMargin)
                .averageOrderValue(averageOrderValue)
                .tableTurnoverRate(BigDecimal.ZERO)
                .inventoryTurnoverRate(BigDecimal.ZERO)
                .labourCostPercentage(BigDecimal.ZERO)
                .foodCostPercentage(BigDecimal.ZERO)
                .build();
    }

    public List<DailySalesReportResponse> getSalesReportRange(
            Long restaurantId, LocalDate startDate, LocalDate endDate) {

        List<SalesReport> reports = salesReportRepository
                .findByRestaurantIdAndReportDateBetween(restaurantId, startDate, endDate);

        return reports.stream()
                .map(this::mapToSalesReportResponse)
                .collect(Collectors.toList());
    }

    public SalesComparisonResponse compareSalesPeriods(
            Long restaurantId,
            LocalDate period1Start, LocalDate period1End,
            LocalDate period2Start, LocalDate period2End) {

        SalesPeriodSummary period1 = calculatePeriodSummary(restaurantId, period1Start, period1End);
        SalesPeriodSummary period2 = calculatePeriodSummary(restaurantId, period2Start, period2End);

        return SalesComparisonResponse.builder()
                .period1(period1)
                .period2(period2)
                .revenueChange(calculatePercentageChange(period1.getTotalRevenue(), period2.getTotalRevenue()))
                .orderCountChange(calculatePercentageChange(
                        new BigDecimal(period1.getTotalOrders()),
                        new BigDecimal(period2.getTotalOrders())))
                .profitChange(calculatePercentageChange(period1.getNetProfit(), period2.getNetProfit()))
                .avgOrderValueChange(calculatePercentageChange(
                        period1.getAverageOrderValue(),
                        period2.getAverageOrderValue()))
                .build();
    }

    // ==================== Popular Items ====================

    @Cacheable(value = "popularItems", key = "#restaurantId + '_' + #period")
    public List<PopularItemResponse> getPopularItems(
            Long restaurantId, String period, int limit) {

        LocalDate startDate = calculatePeriodStartDate(period);
        LocalDate endDate = LocalDate.now();

        List<PopularItem> items = popularItemRepository
                .findTopByRestaurantIdAndPeriod(restaurantId, startDate, endDate, limit);

        return items.stream()
                .map(this::mapToPopularItemResponse)
                .collect(Collectors.toList());
    }

    public PopularItemsAnalysisResponse analyzePopularItems(Long restaurantId, LocalDate startDate, LocalDate endDate) {

        List<PopularItem> allItems = popularItemRepository
                .findByRestaurantIdAndPeriod(restaurantId, startDate, endDate);

        // Calculate total revenue
        BigDecimal totalRevenue = allItems.stream()
                .map(PopularItem::getTotalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Top 10 by revenue
        List<PopularItemResponse> topByRevenue = allItems.stream()
                .sorted((a, b) -> b.getTotalRevenue().compareTo(a.getTotalRevenue()))
                .limit(10)
                .map(this::mapToPopularItemResponse)
                .collect(Collectors.toList());

        // Top 10 by quantity
        List<PopularItemResponse> topByQuantity = allItems.stream()
                .sorted((a, b) -> b.getTotalQuantitySold().compareTo(a.getTotalQuantitySold()))
                .limit(10)
                .map(this::mapToPopularItemResponse)
                .collect(Collectors.toList());

        // Calculate concentration (% of revenue from top 10)
        BigDecimal top10Revenue = topByRevenue.stream()
                .map(PopularItemResponse::getTotalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal concentrationPercentage = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                ? top10Revenue.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;

        return PopularItemsAnalysisResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalItems(allItems.size())
                .totalRevenue(totalRevenue)
                .topByRevenue(topByRevenue)
                .topByQuantity(topByQuantity)
                .revenueConcentration(concentrationPercentage)
                .build();
    }

    public List<ItemPerformanceTrendResponse> getItemPerformanceTrend(
            Long menuItemId, LocalDate startDate, LocalDate endDate) {

        List<PopularItem> trends = popularItemRepository
                .findByMenuItemIdAndDateRange(menuItemId, startDate, endDate);

        return trends.stream()
                .map(item -> ItemPerformanceTrendResponse.builder()
                        .date(item.getAnalysisDate())
                        .quantitySold(item.getTotalQuantitySold())
                        .revenue(item.getTotalRevenue())
                        .orderCount(item.getOrderCount())
                        .averagePrice(item.getAveragePrice())
                        .build())
                .collect(Collectors.toList());
    }

    // ==================== Customer Behavior ====================

    @Cacheable(value = "customerBehavior", key = "#restaurantId + '_' + #date")
    public CustomerBehaviorResponse getCustomerBehavior(Long restaurantId, LocalDate date) {

        CustomerBehavior behavior = customerBehaviorRepository
                .findByRestaurantIdAndAnalysisDate(restaurantId, date)
                .orElse(generateCustomerBehavior(restaurantId, date));

        return mapToCustomerBehaviorResponse(behavior);
    }

    public CustomerSegmentationResponse getCustomerSegmentation(Long restaurantId) {

        List<User> customers = userRepository.findCustomersByRestaurantId(restaurantId);

        Map<com.rms.dto.analytics.CustomerSegment, List<CustomerSegmentData>> segments = new HashMap<>();

        for (User customer : customers) {
            com.rms.dto.analytics.CustomerSegment segment = determineCustomerSegment(customer);
            CustomerSegmentData data = CustomerSegmentData.builder()
                    .customerId(customer.getId())
                    .customerName(customer.getFullName())
                    .totalOrders(getCustomerOrderCount(customer.getId()))
                    .totalSpent(getCustomerTotalSpent(customer.getId()))
                    .avgOrderValue(getCustomerAvgOrderValue(customer.getId()))
                    .lastOrderDate(getCustomerLastOrderDate(customer.getId()))
                    .daysSinceLastOrder(calculateDaysSinceLastOrder(customer.getId()))
                    .build();

            segments.computeIfAbsent(segment, k -> new ArrayList<>()).add(data);
        }

        return CustomerSegmentationResponse.builder()
                .totalCustomers(customers.size())
                .segments(segments)
                .vipCount(segments.getOrDefault(com.rms.dto.analytics.CustomerSegment.VIP, Collections.emptyList()).size())
                .regularCount(segments.getOrDefault(com.rms.dto.analytics.CustomerSegment.REGULAR, Collections.emptyList()).size())
                .occasionalCount(segments.getOrDefault(com.rms.dto.analytics.CustomerSegment.OCCASIONAL, Collections.emptyList()).size())
                .atRiskCount(segments.getOrDefault(com.rms.dto.analytics.CustomerSegment.AT_RISK, Collections.emptyList()).size())
                .lostCount(segments.getOrDefault(com.rms.dto.analytics.CustomerSegment.LOST, Collections.emptyList()).size())
                .build();
    }

    public CustomerLifetimeValueResponse getCustomerLifetimeValue(Long customerId) {

        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        List<Order> orders = orderRepository.findByCustomerId(customerId);

        BigDecimal totalSpent = orders.stream()
                .filter(o -> o.getPaymentStatus() == Order.PaymentStatus.PAID)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long daysSinceFirstOrder = orders.stream()
                .map(Order::getCreatedAt)
                .min(LocalDateTime::compareTo)
                .map(first -> ChronoUnit.DAYS.between(first, LocalDateTime.now()))
                .orElse(0L);

        BigDecimal avgOrderValue = orders.isEmpty()
                ? BigDecimal.ZERO
                : totalSpent.divide(new BigDecimal(orders.size()), 2, RoundingMode.HALF_UP);

        // Calculate predicted CLV (simple model)
        BigDecimal predictedCLV = calculatePredictedCLV(orders, avgOrderValue);

        return CustomerLifetimeValueResponse.builder()
                .customerId(customerId)
                .customerName(customer.getFullName())
                .registeredDate(customer.getCreatedAt().toLocalDate())
                .totalOrders(orders.size())
                .totalSpent(totalSpent)
                .averageOrderValue(avgOrderValue)
                .daysSinceFirstOrder(daysSinceFirstOrder)
                .orderFrequency(calculateOrderFrequency(orders))
                .predictedLifetimeValue(predictedCLV)
                .customerSegment(determineCustomerSegment(customer))
                .build();
    }

    // ==================== Inventory Usage ====================

    public List<InventoryUsageResponse> getInventoryUsage(
            Long restaurantId, LocalDate startDate, LocalDate endDate) {

        List<InventoryUsage> usage = inventoryUsageRepository
                .findByRestaurantIdAndDateRange(restaurantId, startDate, endDate);

        return usage.stream()
                .map(this::mapToInventoryUsageResponse)
                .collect(Collectors.toList());
    }

    public InventoryEfficiencyResponse analyzeInventoryEfficiency(Long restaurantId, LocalDate date) {

        List<InventoryItem> items = inventoryItemRepository.findByRestaurantId(restaurantId);

        int totalItems = items.size();
        int lowStockItems = (int) items.stream()
                .filter(i -> i.getCurrentQuantity().compareTo(i.getMinimumQuantity()) <= 0)
                .count();

        int outOfStock = (int) items.stream()
                .filter(i -> i.getCurrentQuantity().compareTo(BigDecimal.ZERO) == 0)
                .count();

        int overstock = (int) items.stream()
                .filter(i -> i.getMaximumQuantity() != null &&
                        i.getCurrentQuantity().compareTo(i.getMaximumQuantity()) > 0)
                .count();

        BigDecimal totalValue = items.stream()
                .map(i -> i.getCurrentQuantity().multiply(i.getCostPerUnit()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<InventoryUsage> usage = inventoryUsageRepository
                .findByRestaurantIdAndAnalysisDate(restaurantId, date);

        BigDecimal totalUsed = usage.stream()
                .map(InventoryUsage::getQuantityUsed)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalWaste = usage.stream()
                .map(InventoryUsage::getWastageQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal wastePercentage = totalUsed.compareTo(BigDecimal.ZERO) > 0
                ? totalWaste.divide(totalUsed, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;

        return InventoryEfficiencyResponse.builder()
                .analysisDate(date)
                .totalItems(totalItems)
                .lowStockItems(lowStockItems)
                .outOfStockItems(outOfStock)
                .overstockItems(overstock)
                .totalInventoryValue(totalValue)
                .totalQuantityUsed(totalUsed)
                .totalWastage(totalWaste)
                .wastagePercentage(wastePercentage)
                .stockHealthScore(calculateStockHealthScore(lowStockItems, outOfStock, overstock, totalItems))
                .build();
    }

    public List<SlowMovingItemResponse> getSlowMovingItems(Long restaurantId, int daysThreshold) {

        LocalDate cutoffDate = LocalDate.now().minusDays(daysThreshold);

        List<InventoryItem> items = inventoryItemRepository.findByRestaurantId(restaurantId);

        return items.stream()
                .filter(item -> {
                    LocalDate lastUsed = inventoryUsageRepository
                            .findLastUsageDate(item.getId());
                    return lastUsed == null || lastUsed.isBefore(cutoffDate);
                })
                .map(item -> {
                    LocalDate lastUsed = inventoryUsageRepository.findLastUsageDate(item.getId());
                    long daysSinceUse = lastUsed != null
                            ? ChronoUnit.DAYS.between(lastUsed, LocalDate.now())
                            : 999;

                    return SlowMovingItemResponse.builder()
                            .inventoryItemId(item.getId())
                            .itemName(item.getName())
                            .currentQuantity(item.getCurrentQuantity())
                            .unit(item.getUnit() != null ? item.getUnit().name() : null)
                            .lastUsedDate(lastUsed)
                            .daysSinceLastUse(daysSinceUse)
                            .estimatedValue(item.getCurrentQuantity().multiply(item.getCostPerUnit()))
                            .recommendation(generateRecommendation(daysSinceUse, item))
                            .build();
                })
                .sorted(Comparator.comparing(SlowMovingItemResponse::getDaysSinceLastUse).reversed())
                .toList();
    }

    // ==================== Revenue Analytics ====================

    public RevenueAnalyticsResponse getRevenueAnalytics(
            Long restaurantId, LocalDate startDate, LocalDate endDate) {

        List<RevenueAnalytics> analytics = revenueAnalyticsRepository
                .findByRestaurantIdAndDateRange(restaurantId, startDate, endDate);

        BigDecimal totalRevenue = analytics.stream()
                .map(RevenueAnalytics::getTotalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCost = analytics.stream()
                .map(RevenueAnalytics::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netProfit = totalRevenue.subtract(totalCost);

        BigDecimal profitMargin = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                ? netProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;

        return RevenueAnalyticsResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalRevenue(totalRevenue)
                .totalCost(totalCost)
                .netProfit(netProfit)
                .profitMargin(profitMargin)
                .dailyBreakdown(analytics.stream()
                        .map(this::mapToRevenueBreakdown)
                        .collect(Collectors.toList()))
                .revenueByChannel(calculateRevenueByChannel(analytics))
                .revenueByPaymentMethod(calculateRevenueByPaymentMethod(analytics))
                .build();
    }

    public RevenueForecastResponse forecastRevenue(Long restaurantId, int forecastDays) {

        // Get historical data (last 90 days)
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(90);

        List<RevenueAnalytics> history = revenueAnalyticsRepository
                .findByRestaurantIdAndDateRange(restaurantId, startDate, endDate);

        // Simple moving average forecast
        BigDecimal avgDailyRevenue = history.stream()
                .map(RevenueAnalytics::getTotalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(history.size()), 2, RoundingMode.HALF_UP);

        // Calculate trend
        BigDecimal trend = calculateRevenueTrend(history);

        // Generate forecast
        List<RevenueForecastDay> forecast = new ArrayList<>();
        LocalDate forecastDate = LocalDate.now().plusDays(1);

        for (int i = 0; i < forecastDays; i++) {
            BigDecimal projectedRevenue = avgDailyRevenue.add(trend.multiply(new BigDecimal(i)));

            forecast.add(RevenueForecastDay.builder()
                    .date(forecastDate.plusDays(i))
                    .projectedRevenue(projectedRevenue)
                    .confidenceLevel(calculateConfidence(history, i))
                    .build());
        }

        return RevenueForecastResponse.builder()
                .forecastStartDate(forecastDate)
                .forecastEndDate(forecastDate.plusDays(forecastDays - 1))
                .historicalAverage(avgDailyRevenue)
                .trend(trend.compareTo(BigDecimal.ZERO) > 0 ? "UPWARD" : "DOWNWARD")
                .forecast(forecast)
                .totalProjectedRevenue(forecast.stream()
                        .map(RevenueForecastDay::getProjectedRevenue)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .build();
    }

    // ==================== Hourly Analytics ====================

    public HourlyAnalyticsResponse getHourlyAnalytics(Long restaurantId, LocalDate date) {
        List<HourlyBreakdown> hourlyData = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            hourlyData.add(HourlyBreakdown.builder()
                    .hour(hour)
                    .orderCount(0)
                    .revenue(BigDecimal.ZERO)
                    .customerCount(0)
                    .avgOrderValue(BigDecimal.ZERO)
                    .build());
        }

        return HourlyAnalyticsResponse.builder()
                .date(date)
                .hourlyData(hourlyData)
                .peakHour(0)
                .slowestHour(0)
                .peakHourRevenue(BigDecimal.ZERO)
                .build();
    }

    // ==================== Export ====================

    public ReportExportResponse exportReport(Long restaurantId, ReportExportRequest request) {
        String reportId = UUID.randomUUID().toString();
        String format = request.getFormat() != null ? request.getFormat() : "CSV";
        String fileName = String.format("%s_%s.%s",
                request.getReportType() != null ? request.getReportType() : "report",
                LocalDateTime.now().toLocalDate(),
                format.toLowerCase());

        ReportExportResponse response = ReportExportResponse.builder()
                .reportId(reportId)
                .fileName(fileName)
                .downloadUrl(null)
                .fileSize(null)
                .format(format)
                .generatedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        exportReports.put(reportId, response);
        return response;
    }

    public ReportExportResponse getExportStatus(String reportId) {
        return exportReports.getOrDefault(reportId,
                ReportExportResponse.builder()
                        .reportId(reportId)
                        .format(null)
                        .generatedAt(null)
                        .expiresAt(null)
                        .build());
    }

    // ==================== Helper Methods ====================

    private SalesReport generateDailySalesReport(Long restaurantId, LocalDate date) {
        // Generate report on-the-fly if not exists
        // This would typically be done by a scheduled job
        return new SalesReport(); // Placeholder
    }

    private CustomerBehavior generateCustomerBehavior(Long restaurantId, LocalDate date) {
        // Generate behavior analysis on-the-fly
        return new CustomerBehavior(); // Placeholder
    }

    private SalesPeriodSummary calculatePeriodSummary(Long restaurantId, LocalDate start, LocalDate end) {
        List<SalesReport> reports = salesReportRepository
                .findByRestaurantIdAndReportDateBetween(restaurantId, start, end);

        return SalesPeriodSummary.builder()
                .startDate(start)
                .endDate(end)
                .totalOrders(reports.stream().mapToInt(SalesReport::getTotalOrders).sum())
                .totalRevenue(reports.stream()
                        .map(SalesReport::getTotalRevenue)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .netProfit(reports.stream()
                        .map(SalesReport::getNetProfit)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .averageOrderValue(calculateAvgOrderValue(reports))
                .build();
    }

    private BigDecimal calculatePercentageChange(BigDecimal oldValue, BigDecimal newValue) {
        if (oldValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return newValue.subtract(oldValue)
                .divide(oldValue, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    private LocalDate calculatePeriodStartDate(String period) {
        return switch (period.toUpperCase()) {
            case "TODAY" -> LocalDate.now();
            case "WEEK" -> LocalDate.now().minusWeeks(1);
            case "MONTH" -> LocalDate.now().minusMonths(1);
            case "QUARTER" -> LocalDate.now().minusMonths(3);
            case "YEAR" -> LocalDate.now().minusYears(1);
            default -> LocalDate.now().minusDays(7);
        };
    }

    private com.rms.dto.analytics.CustomerSegment determineCustomerSegment(User customer) {
        int orderCount = getCustomerOrderCount(customer.getId());
        BigDecimal totalSpent = getCustomerTotalSpent(customer.getId());
        long daysSinceLastOrder = calculateDaysSinceLastOrder(customer.getId());

        if (orderCount >= 20 && totalSpent.compareTo(new BigDecimal("10000")) > 0) {
            return com.rms.dto.analytics.CustomerSegment.VIP;
        } else if (orderCount >= 10 || daysSinceLastOrder < 14) {
            return com.rms.dto.analytics.CustomerSegment.REGULAR;
        } else if (daysSinceLastOrder < 60) {
            return com.rms.dto.analytics.CustomerSegment.OCCASIONAL;
        } else if (daysSinceLastOrder < 180) {
            return com.rms.dto.analytics.CustomerSegment.AT_RISK;
        } else {
            return com.rms.dto.analytics.CustomerSegment.LOST;
        }
    }

    private int getCustomerOrderCount(Long customerId) {
        return orderRepository.countByCustomerId(customerId);
    }

    private BigDecimal getCustomerTotalSpent(Long customerId) {
        return orderRepository.calculateTotalSpentByCustomer(customerId);
    }

    private BigDecimal getCustomerAvgOrderValue(Long customerId) {
        return orderRepository.calculateAvgOrderValueByCustomer(customerId);
    }

    private LocalDate getCustomerLastOrderDate(Long customerId) {
        return orderRepository.findLastOrderDateByCustomer(customerId);
    }

    private long calculateDaysSinceLastOrder(Long customerId) {
        LocalDate lastOrder = getCustomerLastOrderDate(customerId);
        return lastOrder != null ? ChronoUnit.DAYS.between(lastOrder, LocalDate.now()) : 999;
    }

    private BigDecimal calculatePredictedCLV(List<Order> orders, BigDecimal avgOrderValue) {
        // Simple CLV = Avg Order Value × Order Frequency × Customer Lifespan
        double frequency = calculateOrderFrequency(orders);
        double estimatedLifespan = 365.0; // Assume 1 year

        return avgOrderValue
                .multiply(new BigDecimal(frequency))
                .multiply(new BigDecimal(estimatedLifespan / 365.0));
    }

    private double calculateOrderFrequency(List<Order> orders) {
        if (orders.size() < 2) return 0;

        List<LocalDateTime> orderDates = orders.stream()
                .map(Order::getCreatedAt)
                .sorted()
                .collect(Collectors.toList());

        long daysBetween = ChronoUnit.DAYS.between(
                orderDates.get(0), orderDates.get(orderDates.size() - 1));

        return daysBetween > 0 ? (double) orders.size() / daysBetween * 30 : 0;
    }

    private BigDecimal calculateStockHealthScore(int lowStock, int outOfStock, int overstock, int total) {
        if (total == 0) return BigDecimal.ZERO;

        int healthyItems = total - lowStock - outOfStock - overstock;
        return new BigDecimal(healthyItems)
                .divide(new BigDecimal(total), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    private String generateRecommendation(long daysSinceUse, InventoryItem item) {
        if (daysSinceUse > 180) {
            return "DISCONTINUE - Not used in 6+ months";
        } else if (daysSinceUse > 90) {
            return "REDUCE_STOCK - Low usage";
        } else if (daysSinceUse > 60) {
            return "MONITOR - Usage declining";
        } else {
            return "MAINTAIN - Normal usage";
        }
    }

    private Map<String, BigDecimal> calculateRevenueByChannel(List<RevenueAnalytics> analytics) {
        Map<String, BigDecimal> byChannel = new HashMap<>();

        for (RevenueAnalytics a : analytics) {
            byChannel.merge("DINE_IN", a.getDineInRevenue(), BigDecimal::add);
            byChannel.merge("TAKEAWAY", a.getTakeawayRevenue(), BigDecimal::add);
            byChannel.merge("DELIVERY", a.getDeliveryRevenue(), BigDecimal::add);
        }

        return byChannel;
    }

    private Map<String, BigDecimal> calculateRevenueByPaymentMethod(List<RevenueAnalytics> analytics) {
        Map<String, BigDecimal> byMethod = new HashMap<>();

        for (RevenueAnalytics a : analytics) {
            byMethod.merge("ONLINE", a.getOnlinePaymentRevenue(), BigDecimal::add);
            byMethod.merge("CASH", a.getCashPaymentRevenue(), BigDecimal::add);
            byMethod.merge("WALLET", a.getWalletPaymentRevenue(), BigDecimal::add);
        }

        return byMethod;
    }

    private BigDecimal calculateRevenueTrend(List<RevenueAnalytics> history) {
        if (history.size() < 2) return BigDecimal.ZERO;

        // Simple linear regression
        int n = history.size();
        BigDecimal sumRevenue = history.stream()
                .map(RevenueAnalytics::getTotalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgRevenue = sumRevenue.divide(new BigDecimal(n), 2, RoundingMode.HALF_UP);

        // Calculate trend (simplified)
        BigDecimal recentAvg = history.stream()
                .skip(n - 7)
                .map(RevenueAnalytics::getTotalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(7), 2, RoundingMode.HALF_UP);

        return recentAvg.subtract(avgRevenue);
    }

    private BigDecimal calculateConfidence(List<RevenueAnalytics> history, int daysOut) {
        // Confidence decreases with forecast distance
        BigDecimal baseConfidence = new BigDecimal("95");
        BigDecimal decay = new BigDecimal(daysOut).multiply(new BigDecimal("2"));
        return baseConfidence.subtract(decay).max(new BigDecimal("50"));
    }

    private BigDecimal calculateAvgOrderValue(List<SalesReport> reports) {
        int totalOrders = reports.stream().mapToInt(SalesReport::getTotalOrders).sum();
        if (totalOrders == 0) return BigDecimal.ZERO;

        BigDecimal totalRevenue = reports.stream()
                .map(SalesReport::getTotalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalRevenue.divide(new BigDecimal(totalOrders), 2, RoundingMode.HALF_UP);
    }

    // Mapping methods
    private DailySalesReportResponse mapToSalesReportResponse(SalesReport report) {
        return DailySalesReportResponse.builder()
                .reportDate(report.getReportDate())
                .totalOrders(report.getTotalOrders())
                .totalRevenue(report.getTotalRevenue())
                .netProfit(report.getNetProfit())
                .averageOrderValue(report.getAverageOrderValue())
                .build();
    }

    private PopularItemResponse mapToPopularItemResponse(PopularItem item) {
        return PopularItemResponse.builder()
                .menuItemId(item.getMenuItem().getId())
                .itemName(item.getMenuItem().getName())
                .totalQuantitySold(item.getTotalQuantitySold())
                .totalRevenue(item.getTotalRevenue())
                .orderCount(item.getOrderCount())
                .averagePrice(item.getAveragePrice())
                .revenueShare(item.getRevenueShare())
                .build();
    }

    private CustomerBehaviorResponse mapToCustomerBehaviorResponse(CustomerBehavior behavior) {
        return CustomerBehaviorResponse.builder()
                .analysisDate(behavior.getAnalysisDate())
                .totalCustomers(behavior.getTotalCustomers())
                .newCustomers(behavior.getNewCustomers())
                .returningCustomers(behavior.getReturningCustomers())
                .avgOrdersPerCustomer(behavior.getAvgOrdersPerCustomer())
                .avgSpendPerCustomer(behavior.getAvgSpendPerCustomer())
                .customerRetentionRate(behavior.getCustomerRetentionRate())
                .churnRate(behavior.getChurnRate())
                .build();
    }

    private InventoryUsageResponse mapToInventoryUsageResponse(InventoryUsage usage) {
        return InventoryUsageResponse.builder()
                .inventoryItemId(usage.getInventoryItem().getId())
                .itemName(usage.getInventoryItem().getName())
                .quantityUsed(usage.getQuantityUsed())
                .wastageQuantity(usage.getWastageQuantity())
                .costOfUsage(usage.getCostOfUsage())
                .costOfWastage(usage.getCostOfWastage())
                .build();
    }

    private RevenueBreakdownResponse mapToRevenueBreakdown(RevenueAnalytics analytics) {
        return RevenueBreakdownResponse.builder()
                .date(analytics.getAnalysisDate())
                .totalRevenue(analytics.getTotalRevenue())
                .totalCost(analytics.getTotalCost())
                .netProfit(analytics.getNetProfit())
                .orderCount(analytics.getOrderCount())
                .build();
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private Integer safe(Integer value) {
        return value != null ? value : 0;
    }

    private Long safe(Long value) {
        return value != null ? value : 0L;
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private Integer safe(Integer value) {
        return value != null ? value : 0;
    }

    private Long safe(Long value) {
        return value != null ? value : 0L;
    }
}
