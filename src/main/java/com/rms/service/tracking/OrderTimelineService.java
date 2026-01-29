package com.rms.service.tracking;

// OrderTimelineService.java
//package com.rms.service.tracking;

import com.rms.dto.tracking.OrderTimelineResponse;
import com.rms.entity.Order;
import com.rms.entity.OrderTimeline;
import com.rms.repository.OrderTimelineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderTimelineService {

    private final OrderTimelineRepository timelineRepository;

    public void addEvent(Order order, OrderTimeline.EventType eventType,
                         String title, String description) {

        OrderTimeline event = OrderTimeline.builder()
                .order(order)
                .eventType(eventType)
                .title(title)
                .description(description)
                .timestamp(LocalDateTime.now())
                .icon(getIconForEventType(eventType))
                .isMilestone(isMilestone(eventType))
                .build();

        timelineRepository.save(event);

        log.debug("Added timeline event: {} for order: {}", eventType, order.getOrderNumber());
    }

    public List<OrderTimelineResponse> getTimeline(Long orderId) {
        List<OrderTimeline> events = timelineRepository.findByOrderIdOrderByTimestampDesc(orderId);

        return events.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private String getIconForEventType(OrderTimeline.EventType eventType) {
        return switch (eventType) {
            case ORDER_PLACED -> "🛒";
            case ORDER_CONFIRMED -> "✅";
            case PAYMENT_RECEIVED -> "💳";
            case KITCHEN_STARTED -> "👨‍🍳";
            case FOOD_PREPARING -> "🔥";
            case FOOD_READY -> "✨";
            case DELIVERY_ASSIGNED -> "🚗";
            case DELIVERY_PICKED_UP -> "📦";
            case OUT_FOR_DELIVERY -> "🛵";
            case DELIVERED -> "🎉";
            case CANCELLED -> "❌";
            case ISSUE_REPORTED -> "⚠️";
            case REFUND_PROCESSED -> "💰";
        };
    }

    private boolean isMilestone(OrderTimeline.EventType eventType) {
        return eventType == OrderTimeline.EventType.ORDER_PLACED
                || eventType == OrderTimeline.EventType.ORDER_CONFIRMED
                || eventType == OrderTimeline.EventType.FOOD_READY
                || eventType == OrderTimeline.EventType.DELIVERED;
    }

    private OrderTimelineResponse mapToResponse(OrderTimeline event) {
        return OrderTimelineResponse.builder()
                .id(event.getId())
                .eventType(event.getEventType().name())
                .title(event.getTitle())
                .description(event.getDescription())
                .timestamp(event.getTimestamp())
                .icon(event.getIcon())
                .isMilestone(event.getIsMilestone())
                .displayTime(formatRelativeTime(event.getTimestamp()))
                .build();
    }

    private String formatRelativeTime(LocalDateTime timestamp) {
        Duration duration = Duration.between(timestamp, LocalDateTime.now());

        long minutes = duration.toMinutes();
        if (minutes < 1) return "Just now";
        if (minutes == 1) return "1 minute ago";
        if (minutes < 60) return minutes + " minutes ago";

        long hours = duration.toHours();
        if (hours == 1) return "1 hour ago";
        if (hours < 24) return hours + " hours ago";

        long days = duration.toDays();
        if (days == 1) return "1 day ago";
        return days + " days ago";
    }
}



