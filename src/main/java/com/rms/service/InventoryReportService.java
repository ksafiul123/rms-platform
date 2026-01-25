package com.rms.service;

//package com.rms.service;

import com.rms.dto.InventoryDTO.*;
import com.rms.entity.InventoryItem;
import com.rms.entity.StockTransaction;
import com.rms.repository.InventoryItemRepository;
import com.rms.repository.StockTransactionRepository;
import com.rms.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryReportService {

    private final InventoryItemRepository inventoryRepository;
    private final StockTransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public InventoryReportResponse generateInventoryReport(UserPrincipal currentUser) {
        log.info("Generating inventory report for restaurant {}", currentUser.getRestaurantId());

        List<InventoryItem> allItems = inventoryRepository
                .findByRestaurantIdAndIsActive(currentUser.getRestaurantId(), true);

        InventoryReportResponse report = new InventoryReportResponse();

        // Total counts
        report.setTotalItems((long) allItems.size());
        report.setInStockItems(allItems.stream()
                .filter(i -> i.getStatus() == InventoryItem.InventoryStatus.IN_STOCK)
                .count());
        report.setLowStockItems(allItems.stream()
                .filter(InventoryItem::isLowStock)
                .count());
        report.setOutOfStockItems(allItems.stream()
                .filter(InventoryItem::isOutOfStock)
                .count());

        // Total inventory value
        BigDecimal totalValue = allItems.stream()
                .map(InventoryItem::calculateTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setTotalInventoryValue(totalValue);

        // Category summaries
        Map<InventoryItem.InventoryCategory, List<InventoryItem>> byCategory =
                allItems.stream().collect(Collectors.groupingBy(InventoryItem::getCategory));

        List<CategorySummary> categorySummaries = byCategory.entrySet().stream()
                .map(entry -> {
                    CategorySummary summary = new CategorySummary();
                    summary.setCategory(entry.getKey());
                    summary.setItemCount((long) entry.getValue().size());
                    summary.setTotalValue(entry.getValue().stream()
                            .map(InventoryItem::calculateTotalValue)
                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                    return summary;
                })
                .sorted((a, b) -> b.getTotalValue().compareTo(a.getTotalValue()))
                .collect(Collectors.toList());
        report.setCategorySummaries(categorySummaries);

        // Top value items
        List<InventoryItemResponse> topValueItems = allItems.stream()
                .sorted((a, b) -> b.calculateTotalValue().compareTo(a.calculateTotalValue()))
                .limit(10)
                .map(this::mapToInventoryItemResponse)
                .collect(Collectors.toList());
        report.setTopValueItems(topValueItems);

        // Recently updated
        List<InventoryItemResponse> recentlyUpdated = inventoryRepository
                .findByRestaurantId(currentUser.getRestaurantId(),
                        PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "updatedAt")))
                .getContent()
                .stream()
                .map(this::mapToInventoryItemResponse)
                .collect(Collectors.toList());
        report.setRecentlyUpdated(recentlyUpdated);

        log.info("Inventory report generated: {} total items, ${} total value",
                report.getTotalItems(), report.getTotalInventoryValue());

        return report;
    }

    @Transactional(readOnly = true)
    public BigDecimal calculatePurchaseCost(UserPrincipal currentUser,
                                            LocalDateTime startDate,
                                            LocalDateTime endDate) {
        BigDecimal cost = transactionRepository.calculatePurchaseCost(
                currentUser.getRestaurantId(), startDate, endDate);

        return cost != null ? cost : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public List<StockTransactionResponse> getRecentTransactions(UserPrincipal currentUser,
                                                                int limit) {
        List<StockTransaction> transactions = transactionRepository
                .findByRestaurantId(currentUser.getRestaurantId(),
                        PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent();

        return transactions.stream()
                .map(this::mapToStockTransactionResponse)
                .collect(Collectors.toList());
    }

    private InventoryItemResponse mapToInventoryItemResponse(InventoryItem item) {
        InventoryItemResponse response = new InventoryItemResponse();
        response.setId(item.getId());
        response.setRestaurantId(item.getRestaurantId());
        response.setItemCode(item.getItemCode());
        response.setName(item.getName());
        response.setCategory(item.getCategory());
        response.setUnit(item.getUnit());
        response.setCurrentQuantity(item.getCurrentQuantity());
        response.setMinimumQuantity(item.getMinimumQuantity());
        response.setCostPerUnit(item.getCostPerUnit());
        response.setTotalValue(item.calculateTotalValue());
        response.setStatus(item.getStatus());
        response.setIsLowStock(item.isLowStock());
        response.setIsOutOfStock(item.isOutOfStock());
        return response;
    }

    private StockTransactionResponse mapToStockTransactionResponse(StockTransaction transaction) {
        StockTransactionResponse response = new StockTransactionResponse();
        response.setId(transaction.getId());
        response.setInventoryItemId(transaction.getInventoryItem().getId());
        response.setInventoryItemName(transaction.getInventoryItem().getName());
        response.setTransactionType(transaction.getTransactionType());
        response.setQuantity(transaction.getQuantity());
        response.setQuantityBefore(transaction.getQuantityBefore());
        response.setQuantityAfter(transaction.getQuantityAfter());
        response.setTotalCost(transaction.getTotalCost());
        response.setCreatedAt(transaction.getCreatedAt());
        return response;
    }
}
