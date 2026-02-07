package com.rms.service;

//package com.rms.service;

import com.rms.dto.InventoryDTO.LowStockAlertResponse;
import com.rms.entity.LowStockAlert;
import com.rms.exception.BadRequestException;
import com.rms.exception.ResourceNotFoundException;
import com.rms.repository.LowStockAlertRepository;
import com.rms.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryAlertService {

    private final LowStockAlertRepository alertRepository;

    @Transactional(readOnly = true)
    public Page<LowStockAlertResponse> getAlerts(UserPrincipal currentUser,
                                                 LowStockAlert.AlertStatus status,
                                                 Pageable pageable) {
        Page<LowStockAlert> alerts;

        if (status != null) {
            alerts = alertRepository.findByRestaurantIdAndStatus(
                    currentUser.getRestaurantId(), status, pageable);
        } else {
            alerts = alertRepository.findByRestaurantId(
                    currentUser.getRestaurantId(), pageable);
        }

        return alerts.map(this::mapToAlertResponse);
    }

    @Transactional(readOnly = true)
    public List<LowStockAlertResponse> getActiveAlerts(UserPrincipal currentUser) {
        List<LowStockAlert> alerts = alertRepository
                .findActiveAlerts(currentUser.getRestaurantId());

        return alerts.stream()
                .map(this::mapToAlertResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Long getActiveAlertsCount(UserPrincipal currentUser) {
        return alertRepository.countActiveAlerts(currentUser.getRestaurantId());
    }

    @Transactional
    public LowStockAlertResponse acknowledgeAlert(Long alertId, UserPrincipal currentUser) {
        log.info("Acknowledging alert {} by user {}", alertId, currentUser.getId());

        LowStockAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));

        if (!alert.getRestaurantId().equals(currentUser.getRestaurantId())) {
            throw new ResourceNotFoundException("Alert not found");
        }

        if (alert.getStatus() != LowStockAlert.AlertStatus.ACTIVE) {
            throw new BadRequestException("Alert is not active");
        }

        alert.acknowledge(currentUser.getId());
        LowStockAlert updatedAlert = alertRepository.save(alert);

        log.info("Alert {} acknowledged successfully", alertId);
        return mapToAlertResponse(updatedAlert);
    }

    @Transactional
    public void resolveAlert(Long alertId, UserPrincipal currentUser) {
        log.info("Resolving alert {}", alertId);

        LowStockAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));

        if (!alert.getRestaurantId().equals(currentUser.getRestaurantId())) {
            throw new ResourceNotFoundException("Alert not found");
        }

        alert.resolve();
        alertRepository.save(alert);

        log.info("Alert {} resolved successfully", alertId);
    }

    private LowStockAlertResponse mapToAlertResponse(LowStockAlert alert) {
        LowStockAlertResponse response = new LowStockAlertResponse();
        response.setId(alert.getId());
        response.setInventoryItemId(alert.getInventoryItem().getId());
        response.setInventoryItemName(alert.getInventoryItem().getName());
        response.setItemCode(alert.getInventoryItem().getItemCode());
        response.setAlertType(alert.getAlertType());
        response.setCurrentQuantity(alert.getCurrentQuantity());
        response.setMinimumQuantity(alert.getMinimumQuantity());
        response.setUnit(alert.getInventoryItem().getUnit());
        response.setStatus(alert.getStatus());
        response.setAcknowledgedAt(alert.getAcknowledgedAt());
        response.setResolvedAt(alert.getResolvedAt());
        response.setNotes(alert.getNotes());
        response.setCreatedAt(alert.getCreatedAt());
        return response;
    }
}
