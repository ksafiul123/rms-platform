package com.rms.service;
//package com.rms.service;

import com.rms.dto.InventoryDTO.*;
import com.rms.entity.*;
import com.rms.exception.BadRequestException;
import com.rms.exception.InsufficientStockException;
import com.rms.exception.ResourceNotFoundException;
import com.rms.repository.*;
import com.rms.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryItemRepository inventoryRepository;
    private final MenuItemInventoryRepository menuItemInventoryRepository;
    private final StockTransactionRepository transactionRepository;
    private final LowStockAlertRepository alertRepository;
    private final UserRepository userRepository;

    @Transactional
    public InventoryItemResponse createInventoryItem(CreateInventoryItemRequest request,
                                                     UserPrincipal currentUser) {
        log.info("Creating inventory item {} for restaurant {}",
                request.getItemCode(), currentUser.getRestaurantId());

        // Check if item code already exists
        if (inventoryRepository.existsByRestaurantIdAndItemCode(
                currentUser.getRestaurantId(), request.getItemCode())) {
            throw new BadRequestException("Item code already exists");
        }

        InventoryItem item = new InventoryItem();
        item.setRestaurantId(currentUser.getRestaurantId());
        item.setBranchId(request.getBranchId());
        item.setItemCode(request.getItemCode());
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setCategory(request.getCategory());
        item.setUnit(request.getUnit());
        item.setCurrentQuantity(request.getInitialQuantity());
        item.setMinimumQuantity(request.getMinimumQuantity());
        item.setMaximumQuantity(request.getMaximumQuantity());
        item.setReorderQuantity(request.getReorderQuantity());
        item.setCostPerUnit(request.getCostPerUnit());
        item.setSupplierName(request.getSupplierName());
        item.setSupplierContact(request.getSupplierContact());
        item.setExpiryDate(request.getExpiryDate());
        item.setStorageLocation(request.getStorageLocation());
        item.setNotes(request.getNotes());
        item.setIsActive(true);
        item.updateStatus();

        InventoryItem savedItem = inventoryRepository.save(item);

        // Create initial stock transaction
        if (request.getInitialQuantity().compareTo(BigDecimal.ZERO) > 0) {
            createStockTransaction(
                    savedItem,
                    StockTransaction.TransactionType.MANUAL_ADDITION,
                    request.getInitialQuantity(),
                    request.getCostPerUnit(),
                    currentUser.getId(),
                    "Initial stock",
                    null,
                    null
            );
        }

        log.info("Inventory item {} created successfully", savedItem.getItemCode());
        return mapToInventoryItemResponse(savedItem);
    }

    @Transactional
    public InventoryItemResponse updateInventoryItem(Long itemId, UpdateInventoryItemRequest request,
                                                     UserPrincipal currentUser) {
        log.info("Updating inventory item {}", itemId);

        InventoryItem item = findItemByIdAndRestaurantId(itemId, currentUser.getRestaurantId());

        if (request.getName() != null) item.setName(request.getName());
        if (request.getDescription() != null) item.setDescription(request.getDescription());
        if (request.getCategory() != null) item.setCategory(request.getCategory());
        if (request.getUnit() != null) item.setUnit(request.getUnit());
        if (request.getMinimumQuantity() != null) item.setMinimumQuantity(request.getMinimumQuantity());
        if (request.getMaximumQuantity() != null) item.setMaximumQuantity(request.getMaximumQuantity());
        if (request.getReorderQuantity() != null) item.setReorderQuantity(request.getReorderQuantity());
        if (request.getCostPerUnit() != null) item.setCostPerUnit(request.getCostPerUnit());
        if (request.getSupplierName() != null) item.setSupplierName(request.getSupplierName());
        if (request.getSupplierContact() != null) item.setSupplierContact(request.getSupplierContact());
        if (request.getExpiryDate() != null) item.setExpiryDate(request.getExpiryDate());
        if (request.getStorageLocation() != null) item.setStorageLocation(request.getStorageLocation());
        if (request.getNotes() != null) item.setNotes(request.getNotes());
        if (request.getIsActive() != null) item.setIsActive(request.getIsActive());

        item.updateStatus();
        InventoryItem updatedItem = inventoryRepository.save(item);

        log.info("Inventory item {} updated successfully", itemId);
        return mapToInventoryItemResponse(updatedItem);
    }

    @Transactional
    public void addStock(Long itemId, AddStockRequest request, UserPrincipal currentUser) {
        log.info("Adding stock to inventory item {}: {} units", itemId, request.getQuantity());

        InventoryItem item = findItemByIdAndRestaurantId(itemId, currentUser.getRestaurantId());

        BigDecimal newQuantity = item.getCurrentQuantity().add(request.getQuantity());
        item.setCurrentQuantity(newQuantity);
        item.updateStatus();

        createStockTransaction(
                item,
                request.getTransactionType(),
                request.getQuantity(),
                request.getCostPerUnit() != null ? request.getCostPerUnit() : item.getCostPerUnit(),
                currentUser.getId(),
                request.getNotes(),
                request.getReferenceNumber(),
                request.getSupplier()
        );

        inventoryRepository.save(item);

        // Check if low stock alert should be resolved
        checkAndResolveAlert(item);

        log.info("Stock added successfully. New quantity: {}", newQuantity);
    }

    @Transactional
    public void deductStock(Long itemId, DeductStockRequest request, UserPrincipal currentUser) {
        log.info("Deducting stock from inventory item {}: {} units", itemId, request.getQuantity());

        InventoryItem item = findItemByIdAndRestaurantId(itemId, currentUser.getRestaurantId());

        if (item.getCurrentQuantity().compareTo(request.getQuantity()) < 0) {
            throw new InsufficientStockException(
                    String.format("Insufficient stock. Available: %s, Required: %s",
                            item.getCurrentQuantity(), request.getQuantity()));
        }

        BigDecimal newQuantity = item.getCurrentQuantity().subtract(request.getQuantity());
        item.setCurrentQuantity(newQuantity);
        item.updateStatus();

        createStockTransaction(
                item,
                request.getTransactionType(),
                request.getQuantity().negate(), // Negative for deduction
                item.getCostPerUnit(),
                currentUser.getId(),
                request.getNotes(),
                null,
                null
        );

        inventoryRepository.save(item);

        // Check if low stock alert should be created
        checkAndCreateAlert(item);

        log.info("Stock deducted successfully. New quantity: {}", newQuantity);
    }

    @Transactional
    public void deductStockForOrder(Order order, UserPrincipal currentUser) {
        log.info("Deducting stock for order {}", order.getOrderNumber());

        for (OrderItem orderItem : order.getOrderItems()) {
            List<MenuItemInventory> ingredients = menuItemInventoryRepository
                    .findByMenuItemId(orderItem.getMenuItemId());

            for (MenuItemInventory ingredient : ingredients) {
                BigDecimal requiredQuantity = ingredient.calculateRequiredQuantity(
                        orderItem.getQuantity());

                InventoryItem inventoryItem = ingredient.getInventoryItem();

                // Skip optional ingredients if out of stock
                if (ingredient.getIsOptional() &&
                        inventoryItem.getCurrentQuantity().compareTo(requiredQuantity) < 0) {
                    log.warn("Optional ingredient {} is out of stock, skipping",
                            inventoryItem.getName());
                    continue;
                }

                // Check stock availability
                if (inventoryItem.getCurrentQuantity().compareTo(requiredQuantity) < 0) {
                    throw new InsufficientStockException(
                            String.format("Insufficient stock for %s. Available: %s, Required: %s",
                                    inventoryItem.getName(),
                                    inventoryItem.getCurrentQuantity(),
                                    requiredQuantity));
                }

                // Deduct stock
                BigDecimal newQuantity = inventoryItem.getCurrentQuantity().subtract(requiredQuantity);
                inventoryItem.setCurrentQuantity(newQuantity);
                inventoryItem.updateStatus();

                // Create transaction
                createStockTransaction(
                        inventoryItem,
                        StockTransaction.TransactionType.ORDER_DEDUCTION,
                        requiredQuantity.negate(),
                        inventoryItem.getCostPerUnit(),
                        currentUser.getId(),
                        String.format("Order #%s - %s x%d",
                                order.getOrderNumber(),
                                orderItem.getItemName(),
                                orderItem.getQuantity()),
                        order.getOrderNumber(),
                        null
                );

                // Set order ID
                StockTransaction transaction = inventoryItem.getTransactions().get(
                        inventoryItem.getTransactions().size() - 1);
                transaction.setOrderId(order.getId());

                inventoryRepository.save(inventoryItem);

                // Check and create alert
                checkAndCreateAlert(inventoryItem);
            }
        }

        log.info("Stock deducted successfully for order {}", order.getOrderNumber());
    }

    @Transactional(readOnly = true)
    public StockAvailabilityResponse checkMenuItemAvailability(Long menuItemId,
                                                               int quantity) {
        List<MenuItemInventory> ingredients = menuItemInventoryRepository
                .findByMenuItemId(menuItemId);

        boolean isAvailable = true;
        List<IngredientAvailability> ingredientList = ingredients.stream()
                .map(ingredient -> {
                    BigDecimal required = ingredient.calculateRequiredQuantity(quantity);
                    BigDecimal available = ingredient.getInventoryItem().getCurrentQuantity();
                    boolean hasEnough = available.compareTo(required) >= 0;

                    if (!ingredient.getIsOptional() && !hasEnough) {
                        isAvailable = false;
                    }

                    IngredientAvailability avail = new IngredientAvailability();
                    avail.setInventoryItemId(ingredient.getInventoryItem().getId());
                    avail.setInventoryItemName(ingredient.getInventoryItem().getName());
                    avail.setRequiredQuantity(required);
                    avail.setAvailableQuantity(available);
                    avail.setIsAvailable(hasEnough);
                    avail.setIsOptional(ingredient.getIsOptional());
                    return avail;
                })
                .collect(Collectors.toList());

        StockAvailabilityResponse response = new StockAvailabilityResponse();
        response.setMenuItemId(menuItemId);
        response.setIsAvailable(isAvailable);
        response.setIngredients(ingredientList);
        return response;
    }

    @Transactional(readOnly = true)
    public boolean checkAndReserve(Long menuItemId, int quantity) {
        StockAvailabilityResponse availability = checkMenuItemAvailability(menuItemId, quantity);
        return availability.getIsAvailable();
    }

    @Transactional
    public MenuItemInventoryResponse linkInventoryToMenuItem(Long itemId,
                                                             LinkMenuItemRequest request,
                                                             UserPrincipal currentUser) {
        log.info("Linking inventory item {} to menu item {}", itemId, request.getMenuItemId());

        InventoryItem inventoryItem = findItemByIdAndRestaurantId(
                itemId, currentUser.getRestaurantId());

        // Check if link already exists
        if (menuItemInventoryRepository.existsByMenuItemIdAndInventoryItemId(
                request.getMenuItemId(), itemId)) {
            throw new BadRequestException("This inventory item is already linked to the menu item");
        }

        MenuItemInventory link = new MenuItemInventory();
        link.setMenuItemId(request.getMenuItemId());
        link.setInventoryItem(inventoryItem);
        link.setQuantityRequired(request.getQuantityRequired());
        link.setIsOptional(request.getIsOptional());
        link.setNotes(request.getNotes());

        MenuItemInventory savedLink = menuItemInventoryRepository.save(link);
        log.info("Inventory item linked successfully");

        return mapToMenuItemInventoryResponse(savedLink);
    }

    @Transactional(readOnly = true)
    public InventoryItemResponse getInventoryItem(Long itemId, UserPrincipal currentUser) {
        InventoryItem item = findItemByIdAndRestaurantId(itemId, currentUser.getRestaurantId());
        return mapToInventoryItemResponse(item);
    }

    @Transactional(readOnly = true)
    public Page<InventoryItemResponse> getInventoryItems(UserPrincipal currentUser,
                                                         InventoryItem.InventoryCategory category,
                                                         InventoryItem.InventoryStatus status,
                                                         Pageable pageable) {
        Page<InventoryItem> items;

        if (category != null) {
            items = inventoryRepository.findByRestaurantIdAndCategory(
                    currentUser.getRestaurantId(), category, pageable);
        } else if (status != null) {
            items = inventoryRepository.findByRestaurantIdAndStatus(
                    currentUser.getRestaurantId(), status, pageable);
        } else {
            items = inventoryRepository.findByRestaurantId(
                    currentUser.getRestaurantId(), pageable);
        }

        return items.map(this::mapToInventoryItemResponse);
    }

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> getLowStockItems(UserPrincipal currentUser) {
        List<InventoryItem> items = inventoryRepository
                .findLowStockItems(currentUser.getRestaurantId());

        return items.stream()
                .map(this::mapToInventoryItemResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<StockTransactionResponse> getStockTransactions(Long itemId,
                                                               UserPrincipal currentUser,
                                                               Pageable pageable) {
        InventoryItem item = findItemByIdAndRestaurantId(itemId, currentUser.getRestaurantId());

        Page<StockTransaction> transactions = transactionRepository
                .findByInventoryItemId(item.getId(), pageable);

        return transactions.map(this::mapToStockTransactionResponse);
    }

    private void createStockTransaction(InventoryItem item,
                                        StockTransaction.TransactionType type,
                                        BigDecimal quantity,
                                        BigDecimal costPerUnit,
                                        Long performedBy,
                                        String notes,
                                        String referenceNumber,
                                        String supplier) {
        StockTransaction transaction = new StockTransaction();
        transaction.setInventoryItem(item);
        transaction.setRestaurantId(item.getRestaurantId());
        transaction.setTransactionType(type);
        transaction.setQuantity(quantity);
        transaction.setQuantityBefore(item.getCurrentQuantity().subtract(quantity));
        transaction.setQuantityAfter(item.getCurrentQuantity());
        transaction.setCostPerUnit(costPerUnit);
        transaction.setTotalCost(quantity.abs().multiply(costPerUnit));
        transaction.setPerformedBy(performedBy);
        transaction.setNotes(notes);
        transaction.setReferenceNumber(referenceNumber);
        transaction.setSupplier(supplier);

        item.addTransaction(transaction);
    }

    private void checkAndCreateAlert(InventoryItem item) {
        // Check if alert already exists
        Optional<LowStockAlert> existingAlert = alertRepository
                .findByInventoryItemIdAndStatus(item.getId(), LowStockAlert.AlertStatus.ACTIVE);

        if (existingAlert.isPresent()) {
            return; // Alert already exists
        }

        LowStockAlert alert = null;

        if (item.isOutOfStock()) {
            alert = new LowStockAlert();
            alert.setAlertType(LowStockAlert.AlertType.OUT_OF_STOCK);
        } else if (item.isLowStock()) {
            alert = new LowStockAlert();
            alert.setAlertType(LowStockAlert.AlertType.LOW_STOCK);
        }

        if (alert != null) {
            alert.setInventoryItem(item);
            alert.setRestaurantId(item.getRestaurantId());
            alert.setCurrentQuantity(item.getCurrentQuantity());
            alert.setMinimumQuantity(item.getMinimumQuantity());
            alert.setStatus(LowStockAlert.AlertStatus.ACTIVE);
            alertRepository.save(alert);

            log.warn("Alert created for {}: {}", item.getName(), alert.getAlertType());
        }
    }

    private void checkAndResolveAlert(InventoryItem item) {
        if (!item.isLowStock() && !item.isOutOfStock()) {
            alertRepository.findByInventoryItemIdAndStatus(
                            item.getId(), LowStockAlert.AlertStatus.ACTIVE)
                    .ifPresent(alert -> {
                        alert.resolve();
                        alertRepository.save(alert);
                        log.info("Alert resolved for {}", item.getName());
                    });
        }
    }

    private InventoryItem findItemByIdAndRestaurantId(Long itemId, Long restaurantId) {
        return inventoryRepository.findByIdAndRestaurantId(itemId, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));
    }

    private InventoryItemResponse mapToInventoryItemResponse(InventoryItem item) {
        InventoryItemResponse response = new InventoryItemResponse();
        response.setId(item.getId());
        response.setRestaurantId(item.getRestaurantId());
        response.setBranchId(item.getBranchId());
        response.setItemCode(item.getItemCode());
        response.setName(item.getName());
        response.setDescription(item.getDescription());
        response.setCategory(item.getCategory());
        response.setUnit(item.getUnit());
        response.setCurrentQuantity(item.getCurrentQuantity());
        response.setMinimumQuantity(item.getMinimumQuantity());
        response.setMaximumQuantity(item.getMaximumQuantity());
        response.setReorderQuantity(item.getReorderQuantity());
        response.setCostPerUnit(item.getCostPerUnit());
        response.setTotalValue(item.calculateTotalValue());
        response.setSupplierName(item.getSupplierName());
        response.setSupplierContact(item.getSupplierContact());
        response.setStatus(item.getStatus());
        response.setExpiryDate(item.getExpiryDate());
        response.setStorageLocation(item.getStorageLocation());
        response.setNotes(item.getNotes());
        response.setIsActive(item.getIsActive());
        response.setIsLowStock(item.isLowStock());
        response.setIsOutOfStock(item.isOutOfStock());
        response.setCreatedAt(item.getCreatedAt());
        response.setUpdatedAt(item.getUpdatedAt());
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
        response.setCostPerUnit(transaction.getCostPerUnit());
        response.setTotalCost(transaction.getTotalCost());
        response.setOrderId(transaction.getOrderId());
        response.setReferenceNumber(transaction.getReferenceNumber());
        response.setSupplier(transaction.getSupplier());
        response.setNotes(transaction.getNotes());
        response.setCreatedAt(transaction.getCreatedAt());
        return response;
    }

    private MenuItemInventoryResponse mapToMenuItemInventoryResponse(MenuItemInventory link) {
        MenuItemInventoryResponse response = new MenuItemInventoryResponse();
        response.setId(link.getId());
        response.setMenuItemId(link.getMenuItemId());
        response.setInventoryItemId(link.getInventoryItem().getId());
        response.setInventoryItemName(link.getInventoryItem().getName());
        response.setQuantityRequired(link.getQuantityRequired());
        response.setUnit(link.getInventoryItem().getUnit());
        response.setIsOptional(link.getIsOptional());
        response.setCurrentStock(link.getInventoryItem().getCurrentQuantity());
        response.setIsAvailable(
                link.getInventoryItem().getCurrentQuantity()
                        .compareTo(link.getQuantityRequired()) >= 0);
        response.setNotes(link.getNotes());
        return response;
    }
}
