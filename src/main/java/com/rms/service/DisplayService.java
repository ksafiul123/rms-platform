package com.rms.service;

import com.rms.dto.DisplayConfigResponse;
import com.rms.dto.DisplayDataResponse;
import com.rms.dto.DisplayStatsResponse;
import com.rms.dto.OrderDisplayDetail;
import com.rms.entity.DisplayConfiguration;
import com.rms.entity.KitchenOrderItem;
import com.rms.entity.Order;
import com.rms.entity.OrderDisplaySnapshot;
import com.rms.exception.BadRequestException;
import com.rms.exception.ResourceNotFoundException;
import com.rms.repository.DisplayConfigurationRepository;
import com.rms.repository.OrderDisplaySnapshotRepository;
import com.rms.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class DisplayService {

    private final DisplayConfigurationRepository displayConfigRepository;
    private final OrderDisplaySnapshotRepository snapshotRepository;
    private final OrderRepository orderRepository;
    private final CacheManager cacheManager;

    private final Map<String, SseEmitter> activeEmitters = new ConcurrentHashMap<>();

    @Cacheable(value = "displayData", key = "#displayToken", unless = "#result == null")
    public DisplayDataResponse getLiveOrderData(String displayToken, String mode) {

        DisplayConfiguration config = displayConfigRepository
                .findByDisplayToken(displayToken)
                .orElseThrow(() -> new ResourceNotFoundException("Display not found"));

        if (!config.getIsActive()) {
            throw new BadRequestException("Display is not active");
        }

        // Get current snapshots
        List<OrderDisplaySnapshot> snapshots = snapshotRepository
                .findActiveByRestaurantId(config.getRestaurant().getId());

        // Group by status
        Map<OrderDisplaySnapshot.DisplayStatus, List<OrderDisplaySnapshot>> grouped =
                snapshots.stream()
                        .collect(Collectors.groupingBy(OrderDisplaySnapshot::getDisplayStatus));

        // Build response
        DisplayDataResponse response = DisplayDataResponse.builder()
                .restaurantName(config.getRestaurant().getName())
                .headerText(config.getHeaderText())
                .footerText(config.getFooterText())
                .displayMode(config.getDisplayMode())
                .theme(config.getTheme())
                .currentTime(LocalDateTime.now())
                .serverTimestamp(System.currentTimeMillis())
                .readyOrders(mapToDisplayData(grouped.get(DisplayStatus.READY), config))
                .preparingOrders(mapToDisplayData(grouped.get(OrderDisplaySnapshot.DisplayStatus.PREPARING), config))
                .completedOrders(mapToDisplayData(grouped.get(OrderDisplaySnapshot.DisplayStatus.COLLECTED), config))
                .stats(calculateStats(snapshots))
                .styleConfig(buildStyleConfig(config))
                .build();

        return response;
    }

    public DisplayConfigResponse getDisplayConfig(String displayToken) {
        DisplayConfiguration config = displayConfigRepository
                .findByDisplayToken(displayToken)
                .orElseThrow(() -> new ResourceNotFoundException("Display not found"));

        DisplayConfigResponse.StyleConfig styleConfig = DisplayConfigResponse.StyleConfig.builder()
                .primaryColor(config.getPrimaryColor())
                .secondaryColor(config.getSecondaryColor())
                .readyColor(config.getReadyColor())
                .preparingColor(config.getPreparingColor())
                .fontFamily(config.getFontFamily())
                .logoUrl(config.getLogoUrl())
                .backgroundImageUrl(config.getBackgroundImageUrl())
                .build();

        DisplayConfigResponse.TextConfig textConfig = DisplayConfigResponse.TextConfig.builder()
                .headerText(config.getHeaderText())
                .footerText(config.getFooterText())
                .language(config.getLanguage())
                .translations(Map.of())
                .build();

        return DisplayConfigResponse.builder()
                .restaurantId(config.getRestaurant().getId())
                .restaurantName(config.getRestaurant().getName())
                .displayMode(config.getDisplayMode())
                .theme(config.getTheme())
                .refreshIntervalSeconds(config.getRefreshIntervalSeconds())
                .showPreparing(config.getShowPreparing())
                .showReady(config.getShowReady())
                .showCompleted(config.getShowCompleted())
                .maxOrdersDisplay(config.getMaxOrdersDisplay())
                .showOrderItems(config.getShowOrderItems())
                .showEstimatedTime(config.getShowEstimatedTime())
                .showElapsedTime(config.getShowElapsedTime())
                .style(styleConfig)
                .text(textConfig)
                .build();
    }

    public DisplayStatsResponse getDisplayStats(String displayToken) {
        DisplayConfiguration config = displayConfigRepository
                .findByDisplayToken(displayToken)
                .orElseThrow(() -> new ResourceNotFoundException("Display not found"));

        List<OrderDisplaySnapshot> snapshots = snapshotRepository
                .findActiveByRestaurantId(config.getRestaurant().getId());

        long readyCount = snapshots.stream()
                .filter(snapshot -> snapshot.getDisplayStatus() == OrderDisplaySnapshot.DisplayStatus.READY)
                .count();
        long preparingCount = snapshots.stream()
                .filter(snapshot -> snapshot.getDisplayStatus() == OrderDisplaySnapshot.DisplayStatus.PREPARING)
                .count();
        long completedCount = snapshots.stream()
                .filter(snapshot -> snapshot.getDisplayStatus() == OrderDisplaySnapshot.DisplayStatus.COLLECTED)
                .count();

        List<Integer> elapsedTimes = snapshots.stream()
                .map(OrderDisplaySnapshot::getElapsedMinutes)
                .filter(value -> value != null && value >= 0)
                .toList();

        Integer avgPreparationTime = elapsedTimes.isEmpty()
                ? null
                : (int) Math.round(elapsedTimes.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0));

        Integer peakWaitTime = elapsedTimes.isEmpty()
                ? null
                : elapsedTimes.stream()
                .max(Integer::compareTo)
                .orElse(null);

        return DisplayStatsResponse.builder()
                .totalOrders(snapshots.size())
                .readyCount((int) readyCount)
                .preparingCount((int) preparingCount)
                .completedCount((int) completedCount)
                .avgPreparationTime(avgPreparationTime)
                .peakWaitTime(peakWaitTime)
                .build();
    }

    public OrderDisplayDetail getOrderDetail(String displayToken, String orderNumber) {
        DisplayConfiguration config = displayConfigRepository
                .findByDisplayToken(displayToken)
                .orElseThrow(() -> new ResourceNotFoundException("Display not found"));

        Order order = orderRepository
                .findByOrderNumberAndRestaurantId(orderNumber, config.getRestaurant().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        List<OrderDisplayDetail.OrderItemDetail> items = order.getOrderItems().stream()
                .map(item -> OrderDisplayDetail.OrderItemDetail.builder()
                        .itemName(item.getItemName())
                        .quantity(item.getQuantity())
                        .specialInstructions(item.getSpecialInstructions())
                        .build())
                .toList();

        return OrderDisplayDetail.builder()
                .orderNumber(order.getOrderNumber())
                .displayNumber(extractDisplayNumber(order.getOrderNumber()))
                .tableNumber(order.getTableNumber())
                .orderType(order.getOrderType().name())
                .status(order.getStatus().name())
                .customerName(null)
                .totalItems(order.getOrderItems().size())
                .estimatedReadyTime(order.getEstimatedReadyTime())
                .actualReadyTime(order.getActualReadyTime())
                .elapsedMinutes(calculateElapsedMinutes(order))
                .remainingMinutes(calculateRemainingMinutes(order))
                .items(items)
                .build();
    }

    @Scheduled(fixedDelay = 5000) // Every 5 seconds
    @Async
    public void refreshOrderSnapshots() {

        List<DisplayConfiguration> activeConfigs = displayConfigRepository
                .findByIsActiveTrue();

        for (DisplayConfiguration config : activeConfigs) {
            try {
                updateSnapshotsForRestaurant(config.getRestaurant().getId());

                // Notify SSE clients
                notifyDisplayClients(config.getDisplayToken());

            } catch (Exception e) {
                log.error("Error refreshing snapshots for restaurant: {}",
                        config.getRestaurant().getId(), e);
            }
        }
    }

    private void updateSnapshotsForRestaurant(Long restaurantId) {

        // Get active orders
        List<Order> activeOrders = orderRepository
                .findActiveOrdersForDisplay(restaurantId);

        // Update or create snapshots
        for (Order order : activeOrders) {
            OrderDisplaySnapshot snapshot = snapshotRepository
                    .findByOrderId(order.getId())
                    .orElse(new OrderDisplaySnapshot());

            snapshot.setRestaurant(order.getRestaurant());
            snapshot.setOrder(order);
            snapshot.setOrderNumber(order.getOrderNumber());
            snapshot.setDisplayNumber(extractDisplayNumber(order.getOrderNumber()));
            snapshot.setTableNumber(order.getTableNumber());
            snapshot.setOrderType(order.getOrderType());
            snapshot.setDisplayStatus(mapOrderStatusToDisplayStatus(order.getStatus()));
            snapshot.setTotalItems(order.getOrderItems().size());
            snapshot.setItemsCompleted(calculateCompletedItems(order));
            snapshot.setEstimatedReadyTime(order.getEstimatedReadyTime());
            snapshot.setActualReadyTime(order.getActualReadyTime());
            snapshot.setElapsedMinutes(calculateElapsedMinutes(order));
            snapshot.setRemainingMinutes(calculateRemainingMinutes(order));
            snapshot.setLastUpdated(LocalDateTime.now());

            // Serialize items if needed
            if (order.getOrderItems() != null) {
                snapshot.setItemsJson(serializeOrderItems(order.getOrderItems()));
            }

            // Handle highlighting for newly ready orders
            if (snapshot.getDisplayStatus() == OrderDisplaySnapshot.DisplayStatus.READY
                    && !snapshot.getIsHighlighted()) {
                snapshot.setIsHighlighted(true);
                snapshot.setHighlightedAt(LocalDateTime.now());
            }

            snapshotRepository.save(snapshot);
        }

        // Clear cache for this restaurant
        evictDisplayCache(restaurantId);
    }

    private OrderDisplaySnapshot.DisplayStatus mapOrderStatusToDisplayStatus(Order.OrderStatus orderStatus) {
        return switch (orderStatus) {
            case CONFIRMED, PREPARING -> OrderDisplaySnapshot.DisplayStatus.PREPARING;
            case READY -> OrderDisplaySnapshot.DisplayStatus.READY;
            case DELIVERED, COMPLETED -> OrderDisplaySnapshot.DisplayStatus.COLLECTED;
            default -> OrderDisplaySnapshot.DisplayStatus.HIDDEN;
        };
    }

    public SseEmitter createDisplayStream(String displayToken) {

        DisplayConfiguration config = displayConfigRepository
                .findByDisplayToken(displayToken)
                .orElseThrow(() -> new ResourceNotFoundException("Display not found"));

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        activeEmitters.put(displayToken, emitter);

        emitter.onCompletion(() -> activeEmitters.remove(displayToken));
        emitter.onTimeout(() -> activeEmitters.remove(displayToken));
        emitter.onError(e -> activeEmitters.remove(displayToken));

        // Send initial data
        try {
            DisplayDataResponse data = getLiveOrderData(displayToken, null);
            emitter.send(SseEmitter.event()
                    .name("order-update")
                    .data(data));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }

    private void notifyDisplayClients(String displayToken) {

        SseEmitter emitter = activeEmitters.get(displayToken);
        if (emitter != null) {
            try {
                DisplayDataResponse data = getLiveOrderData(displayToken, null);
                emitter.send(SseEmitter.event()
                        .name("order-update")
                        .data(data));
            } catch (IOException e) {
                activeEmitters.remove(displayToken);
                emitter.completeWithError(e);
            }
        }
    }

    private String extractDisplayNumber(String orderNumber) {
        // Extract numeric portion for display
        // ORD20240202001 -> 001
        String numeric = orderNumber.replaceAll("\\D+", "");
        return numeric.substring(Math.max(0, numeric.length() - 3));
    }

    private Integer calculateCompletedItems(Order order) {
        // Logic to count completed kitchen items
        return (int) order.getKitchenItems().stream()
                .filter(item -> item.getStatus() == KitchenOrderItem.ItemStatus.COMPLETED)
                .count();
    }

    private Integer calculateElapsedMinutes(Order order) {
        if (order.getCreatedAt() == null) return 0;
        return (int) Duration.between(order.getCreatedAt(), LocalDateTime.now())
                .toMinutes();
    }

    private Integer calculateRemainingMinutes(Order order) {
        if (order.getEstimatedReadyTime() == null) return null;
        long remaining = Duration.between(LocalDateTime.now(), order.getEstimatedReadyTime())
                .toMinutes();
        return (int) Math.max(0, remaining);
    }
}
