package com.rms.service.analytics;

import com.rms.entity.*;
import com.rms.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsSchedulerService {

    private final RestaurantRepository restaurantRepository;
    private final OrderRepository orderRepository;
    private final SalesReportRepository salesReportRepository;
    private final PopularItemRepository popularItemRepository;
    private final CustomerBehaviorRepository customerBehaviorRepository;
    private final InventoryUsageRepository inventoryUsageRepository;
    private final RevenueAnalyticsRepository revenueAnalyticsRepository;
    private final UserRepository userRepository;
    private final InventoryItemRepository inventoryItemRepository;

    /**
     * Generate daily sales reports for all restaurants
     * Runs at 2 AM every day
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void generateDailySalesReports() {
        log.info("Starting daily sales report generation");

        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<Restaurant> restaurants = restaurantRepository.findAll();

        for (Restaurant restaurant : restaurants) {
            try {
                generateSalesReportForDate(restaurant, yesterday);
                log.info("Generated sales report for restaurant: {} on date: {}",
                        restaurant.getName(), yesterday);
            } catch (Exception e) {
                log.error("Failed to generate sales report for restaurant: {}",
                        restaurant.getId(), e);
            }
        }

        log.info("Completed daily sales report generation");
    }

    /**
     * Generate popular items analysis
     * Runs at 3 AM every day
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void generatePopularItemsAnalysis() {
        log.info("Starting popular items analysis");

        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<Restaurant> restaurants = restaurantRepository.findAll();

        for (Restaurant restaurant : restaurants) {
            try {
                generatePopularItemsForDate(restaurant, yesterday);
                log.info("Generated popular items for restaurant: {} on date: {}",
                        restaurant.getName(), yesterday);
            } catch (Exception e) {
                log.error("Failed to generate popular items for restaurant: {}",
                        restaurant.getId(), e);
            }
        }

        log.info("Completed popular items analysis");
    }

    /**
     * Generate customer behavior analytics
     * Runs at 4 AM every day
     */
    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void generateCustomerBehaviorAnalytics() {
        log.info("Starting customer behavior analytics");

        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<Restaurant> restaurants = restaurantRepository.findAll();

        for (Restaurant restaurant : restaurants) {
            try {
                generateCustomerBehaviorForDate(restaurant, yesterday);
                log.info("Generated customer behavior for restaurant: {} on date: {}",
                        restaurant.getName(), yesterday);
            } catch (Exception e) {
                log.error("Failed to generate customer behavior for restaurant: {}",
                        restaurant.getId(), e);
            }
        }

        log.info("Completed customer behavior analytics");
    }

    /**
     * Generate inventory usage analytics
     * Runs at 5 AM every day
     */
    @Scheduled(cron = "0 0 5 * * *")
    @Transactional
    public void generateInventoryUsageAnalytics() {
        log.info("Starting inventory usage analytics");

        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<Restaurant> restaurants = restaurantRepository.findAll();

        for (Restaurant restaurant : restaurants) {
            try {
                generateInventoryUsageForDate(restaurant, yesterday);
                log.info("Generated inventory usage for restaurant: {} on date: {}",
                        restaurant.getName(), yesterday);
            } catch (Exception e) {
                log.error("Failed to generate inventory usage for restaurant: {}",
                        restaurant.getId(), e);
            }
        }

        log.info("Completed inventory usage analytics");
    }

    /**
     * Generate revenue analytics
     * Runs at 6 AM every day
     */
    @Scheduled(cron = "0 0 6 * * *")
    @Transactional
    public void generateRevenueAnalytics() {
        log.info("Starting revenue analytics");

        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<Restaurant> restaurants = restaurantRepository.findAll();

        for (Restaurant restaurant : restaurants) {
            try {
                generateRevenueAnalyticsForDate(restaurant, yesterday);
                log.info("Generated revenue analytics for restaurant: {} on date: {}",
                        restaurant.getName(), yesterday);
            } catch (Exception e) {
                log.error("Failed to generate revenue analytics for restaurant: {}",
                        restaurant.getId(), e);
            }
        }

        log.info("Completed revenue analytics");
    }

    // ==================== Helper Methods ====================

    private void generateSalesReportForDate(Restaurant restaurant, LocalDate date) {

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        // Get orders for the date
        List<Order> orders = orderRepository.findByRestaurantIdAndCreatedAtBetween(
                restaurant.getId(), startOfDay, endOfDay);

        // Filter completed orders
        List<Order> completedOrders = orders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.COMPLETED ||
                        o.getStatus() == Order.OrderStatus.DELIVERED)
                .collect(Collectors.toList());

        // Calculate metrics
        Integer totalOrders = completedOrders.size();

        BigDecimal totalRevenue = completedOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCost = completedOrders.stream()
                .map(o -> calculateOrderCost(o))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netProfit = totalRevenue.subtract(totalCost);

        BigDecimal avgOrderValue = totalOrders > 0
                ? totalRevenue.divide(new BigDecimal(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal totalDiscounts = completedOrders.stream()
                .map(Order::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalTaxes = completedOrders.stream()
                .map(Order::getTaxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Group by order type
        Map<Order.OrderType, List<Order>> byType = completedOrders.stream()
                .collect(Collectors.groupingBy(Order::getOrderType));

        Integer dineInOrders = byType.getOrDefault(Order.OrderType.DINE_IN, List.of()).size();
        BigDecimal dineInRevenue = byType.getOrDefault(Order.OrderType.DINE_IN, List.of()).stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer takeawayOrders = byType.getOrDefault(Order.OrderType.TAKEAWAY, List.of()).size();
        BigDecimal takeawayRevenue = byType.getOrDefault(Order.OrderType.TAKEAWAY, List.of()).stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer deliveryOrders = byType.getOrDefault(Order.OrderType.DELIVERY, List.of()).size();
        BigDecimal deliveryRevenue = byType.getOrDefault(Order.OrderType.DELIVERY, List.of()).stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Payment methods
        BigDecimal onlinePayments = completedOrders.stream()
                .filter(o -> o.getPayment() != null &&
                        o.getPayment().getPaymentMethod() == Payment.PaymentMethod.ONLINE_CARD)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal cashPayments = completedOrders.stream()
                .filter(o -> o.getPayment() != null &&
                        o.getPayment().getPaymentMethod() == Payment.PaymentMethod.CASH)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal walletPayments = completedOrders.stream()
                .filter(o -> o.getPayment() != null &&
                        o.getPayment().getPaymentMethod() == Payment.PaymentMethod.WALLET)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Customer metrics
        long uniqueCustomers = completedOrders.stream()
                .map(Order::getCustomer)
                .distinct()
                .count();

        long newCustomers = completedOrders.stream()
                .map(Order::getCustomer)
                .filter(c -> isNewCustomer(c, date))
                .distinct()
                .count();

        long returningCustomers = uniqueCustomers - newCustomers;

        // Cancelled orders
        long cancelledOrders = orders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.CANCELLED)
                .count();

        BigDecimal refundedAmount = orders.stream()
                .filter(o -> o.getPaymentStatus() == Order.PaymentStatus.REFUNDED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create or update sales report
        SalesReport report = salesReportRepository
                .findByRestaurantIdAndReportDate(restaurant.getId(), date)
                .orElse(new SalesReport());

        report.setRestaurant(restaurant);
        report.setReportDate(date);
        report.setTotalOrders(totalOrders);
        report.setTotalRevenue(totalRevenue);
        report.setTotalCost(totalCost);
        report.setNetProfit(netProfit);
        report.setAverageOrderValue(avgOrderValue);
        report.setTotalDiscounts(totalDiscounts);
        report.setTotalTaxes(totalTaxes);
        report.setDineInOrders(dineInOrders);
        report.setDineInRevenue(dineInRevenue);
        report.setTakeawayOrders(takeawayOrders);
        report.setTakeawayRevenue(takeawayRevenue);
        report.setDeliveryOrders(deliveryOrders);
        report.setDeliveryRevenue(deliveryRevenue);
        report.setOnlinePayments(onlinePayments);
        report.setCashPayments(cashPayments);
        report.setWalletPayments(walletPayments);
        report.setUniqueCustomers((int) uniqueCustomers);
        report.setNewCustomers((int) newCustomers);
        report.setReturningCustomers((int) returningCustomers);
        report.setCancelledOrders((int) cancelledOrders);
        report.setRefundedAmount(refundedAmount);

        salesReportRepository.save(report);
    }

    private void generatePopularItemsForDate(Restaurant restaurant, LocalDate date) {

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        List<Order> completedOrders = orderRepository
                .findCompletedByRestaurantIdAndDateRange(restaurant.getId(), startOfDay, endOfDay);

        // Aggregate by menu item
        Map<MenuItem, List<OrderItem>> itemSales = completedOrders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .collect(Collectors.groupingBy(OrderItem::getMenuItem));

        // Create PopularItem records
        for (Map.Entry<MenuItem, List<OrderItem>> entry : itemSales.entrySet()) {
            MenuItem menuItem = entry.getKey();
            List<OrderItem> orderItems = entry.getValue();

            int totalQuantity = orderItems.stream()
                    .mapToInt(OrderItem::getQuantity)
                    .sum();

            BigDecimal totalRevenue = orderItems.stream()
                    .map(oi -> oi.getPrice().multiply(new BigDecimal(oi.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            int orderCount = (int) orderItems.stream()
                    .map(OrderItem::getOrder)
                    .distinct()
                    .count();

            BigDecimal avgPrice = totalRevenue.divide(
                    new BigDecimal(totalQuantity), 2, RoundingMode.HALF_UP);

            PopularItem popularItem = popularItemRepository
                    .findByRestaurantIdAndMenuItemIdAndAnalysisDate(
                            restaurant.getId(), menuItem.getId(), date)
                    .orElse(new PopularItem());

            popularItem.setRestaurant(restaurant);
            popularItem.setMenuItem(menuItem);
            popularItem.setAnalysisDate(date);
            popularItem.setTotalQuantitySold(totalQuantity);
            popularItem.setTotalRevenue(totalRevenue);
            popularItem.setOrderCount(orderCount);
            popularItem.setAveragePrice(avgPrice);

            popularItemRepository.save(popularItem);
        }
    }

    private void generateCustomerBehaviorForDate(Restaurant restaurant, LocalDate date) {

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        List<Order> orders = orderRepository.findByRestaurantIdAndCreatedAtBetween(
                restaurant.getId(), startOfDay, endOfDay);

        // Calculate metrics
        long totalCustomers = userRepository.countCustomersByRestaurantId(restaurant.getId());

        long newCustomers = orders.stream()
                .map(Order::getCustomer)
                .filter(c -> isNewCustomer(c, date))
                .distinct()
                .count();

        long returningCustomers = orders.stream()
                .map(Order::getCustomer)
                .filter(c -> !isNewCustomer(c, date))
                .distinct()
                .count();

        // More calculations...

        CustomerBehavior behavior = customerBehaviorRepository
                .findByRestaurantIdAndAnalysisDate(restaurant.getId(), date)
                .orElse(new CustomerBehavior());

        behavior.setRestaurant(restaurant);
        behavior.setAnalysisDate(date);
        behavior.setTotalCustomers((int) totalCustomers);
        behavior.setNewCustomers((int) newCustomers);
        behavior.setReturningCustomers((int) returningCustomers);
        // Set other fields...

        customerBehaviorRepository.save(behavior);
    }

    private void generateInventoryUsageForDate(Restaurant restaurant, LocalDate date) {
        // Implementation for inventory usage analytics
    }

    private void generateRevenueAnalyticsForDate(Restaurant restaurant, LocalDate date) {
        // Implementation for revenue analytics
    }

    private BigDecimal calculateOrderCost(Order order) {
        // Calculate cost based on ingredients used
        return order.getOrderItems().stream()
                .map(item -> calculateItemCost(item))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateItemCost(OrderItem orderItem) {
        // Get ingredient costs from inventory
        return BigDecimal.ZERO; // Placeholder
    }

    private boolean isNewCustomer(User customer, LocalDate date) {
        LocalDate registrationDate = customer.getCreatedAt().toLocalDate();
        return registrationDate.equals(date) || registrationDate.plusDays(7).isAfter(date);
    }
}
