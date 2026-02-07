package com.rms.controller;

import com.rms.dto.DisplayConfigResponse;
import com.rms.dto.DisplayDataResponse;
import com.rms.dto.DisplayStatsResponse;
import com.rms.dto.OrderDisplayDetail;
import com.rms.service.DisplayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/public/display")
@RequiredArgsConstructor
public class PublicDisplayController {

    private final DisplayService displayService;

    /**
     * Get live order data for public display
     * No authentication required - uses display token
     */
    @GetMapping("/{displayToken}/orders")
    public ResponseEntity<DisplayDataResponse> getLiveOrders(
            @PathVariable String displayToken,
            @RequestParam(required = false) String mode) {

        DisplayDataResponse response = displayService.getLiveOrderData(displayToken, mode);
        return ResponseEntity.ok(response);
    }

    /**
     * Get display configuration
     */
    @GetMapping("/{displayToken}/config")
    public ResponseEntity<DisplayConfigResponse> getDisplayConfig(
            @PathVariable String displayToken) {

        DisplayConfigResponse config = displayService.getDisplayConfig(displayToken);
        return ResponseEntity.ok(config);
    }

    /**
     * Get order count by status
     */
    @GetMapping("/{displayToken}/stats")
    public ResponseEntity<DisplayStatsResponse> getDisplayStats(
            @PathVariable String displayToken) {

        DisplayStatsResponse stats = displayService.getDisplayStats(displayToken);
        return ResponseEntity.ok(stats);
    }

    /**
     * SSE endpoint for real-time updates
     */
    @GetMapping(value = "/{displayToken}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamOrderUpdates(@PathVariable String displayToken) {

        return displayService.createDisplayStream(displayToken);
    }

    /**
     * Get order details for an announcement
     */
    @GetMapping("/{displayToken}/orders/{orderNumber}")
    public ResponseEntity<OrderDisplayDetail> getOrderDetail(
            @PathVariable String displayToken,
            @PathVariable String orderNumber) {

        OrderDisplayDetail detail = displayService.getOrderDetail(displayToken, orderNumber);
        return ResponseEntity.ok(detail);
    }
}
