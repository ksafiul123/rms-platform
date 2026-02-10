package com.rms.service;

//package com.rms.service;

import com.rms.dto.InventoryDTO.*;
import com.rms.entity.*;
import com.rms.exception.BadRequestException;
import com.rms.exception.InsufficientStockException;
import com.rms.exception.ResourceNotFoundException;
import com.rms.repository.*;
import com.rms.security.UserPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryItemRepository inventoryRepository;

    @Mock
    private MenuItemInventoryRepository menuItemInventoryRepository;

    @Mock
    private StockTransactionRepository transactionRepository;

    @Mock
    private LowStockAlertRepository alertRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private UserPrincipal adminPrincipal;

    @BeforeEach
    void setUp() {
        adminPrincipal = createUserPrincipal(1L, 100L, List.of("RESTAURANT_ADMIN"));
    }

    @Test
    void createInventoryItem_Success() {
        // Arrange
        CreateInventoryItemRequest request = new CreateInventoryItemRequest();
        request.setItemCode("TOMATO-001");
        request.setName("Fresh Tomatoes");
        request.setCategory(InventoryItem.InventoryCategory.VEGETABLES);
        request.setUnit(InventoryItem.Unit.KG);
        request.setInitialQuantity(new BigDecimal("50.00"));
        request.setMinimumQuantity(new BigDecimal("10.00"));
        request.setCostPerUnit(new BigDecimal("2.50"));

        when(inventoryRepository.existsByRestaurantIdAndItemCode(100L, "TOMATO-001"))
                .thenReturn(false);
        when(inventoryRepository.save(any(InventoryItem.class))).thenAnswer(i -> {
            InventoryItem item = i.getArgument(0);
            item.setId(1L);
            return item;
        });

        // Act
        InventoryItemResponse response = inventoryService.createInventoryItem(
                request, adminPrincipal);

        // Assert
        assertNotNull(response);
        assertEquals("TOMATO-001", response.getItemCode());
        assertEquals("Fresh Tomatoes", response.getName());
        assertEquals(new BigDecimal("50.00"), response.getCurrentQuantity());
        verify(inventoryRepository).save(any(InventoryItem.class));
    }

    @Test
    void createInventoryItem_DuplicateItemCode_ThrowsBadRequest() {
        // Arrange
        CreateInventoryItemRequest request = new CreateInventoryItemRequest();
        request.setItemCode("TOMATO-001");
        request.setName("Fresh Tomatoes");
        request.setCategory(InventoryItem.InventoryCategory.VEGETABLES);
        request.setUnit(InventoryItem.Unit.KG);
        request.setInitialQuantity(new BigDecimal("50.00"));
        request.setMinimumQuantity(new BigDecimal("10.00"));
        request.setCostPerUnit(new BigDecimal("2.50"));

        when(inventoryRepository.existsByRestaurantIdAndItemCode(100L, "TOMATO-001"))
                .thenReturn(true);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> inventoryService.createInventoryItem(request, adminPrincipal));
    }

    @Test
    void addStock_Success() {
        // Arrange
        InventoryItem item = createMockInventoryItem(1L, new BigDecimal("20.00"));
        AddStockRequest request = new AddStockRequest();
        request.setQuantity(new BigDecimal("30.00"));
        request.setTransactionType(StockTransaction.TransactionType.PURCHASE);
        request.setCostPerUnit(new BigDecimal("2.50"));

        when(inventoryRepository.findByIdAndRestaurantId(1L, 100L))
                .thenReturn(Optional.of(item));
        when(inventoryRepository.save(any(InventoryItem.class))).thenReturn(item);

        // Act
        inventoryService.addStock(1L, request, adminPrincipal);

        // Assert
        assertEquals(new BigDecimal("50.00"), item.getCurrentQuantity());
        verify(inventoryRepository).save(item);
    }

    @Test
    void deductStock_Success() {
        // Arrange
        InventoryItem item = createMockInventoryItem(1L, new BigDecimal("50.00"));
        DeductStockRequest request = new DeductStockRequest();
        request.setQuantity(new BigDecimal("15.00"));
        request.setTransactionType(StockTransaction.TransactionType.MANUAL_DEDUCTION);

        when(inventoryRepository.findByIdAndRestaurantId(1L, 100L))
                .thenReturn(Optional.of(item));
        when(inventoryRepository.save(any(InventoryItem.class))).thenReturn(item);

        // Act
        inventoryService.deductStock(1L, request, adminPrincipal);

        // Assert
        assertEquals(new BigDecimal("35.00"), item.getCurrentQuantity());
        verify(inventoryRepository).save(item);
    }

    @Test
    void deductStock_InsufficientStock_ThrowsException() {
        // Arrange
        InventoryItem item = createMockInventoryItem(1L, new BigDecimal("10.00"));
        DeductStockRequest request = new DeductStockRequest();
        request.setQuantity(new BigDecimal("15.00"));
        request.setTransactionType(StockTransaction.TransactionType.MANUAL_DEDUCTION);

        when(inventoryRepository.findByIdAndRestaurantId(1L, 100L))
                .thenReturn(Optional.of(item));

        // Act & Assert
        assertThrows(InsufficientStockException.class,
                () -> inventoryService.deductStock(1L, request, adminPrincipal));
    }

    @Test
    void deductStockForOrder_Success() {
        // Arrange
        Order order = createMockOrder();
        InventoryItem tomatoes = createMockInventoryItem(1L, new BigDecimal("100.00"));
        InventoryItem cheese = createMockInventoryItem(2L, new BigDecimal("50.00"));

        MenuItemInventory link1 = new MenuItemInventory();
        link1.setInventoryItem(tomatoes);
        link1.setQuantityRequired(new BigDecimal("0.5"));
        link1.setIsOptional(false);

        MenuItemInventory link2 = new MenuItemInventory();
        link2.setInventoryItem(cheese);
        link2.setQuantityRequired(new BigDecimal("0.2"));
        link2.setIsOptional(false);

        when(menuItemInventoryRepository.findByMenuItemId(1L))
                .thenReturn(List.of(link1, link2));
        when(inventoryRepository.save(any(InventoryItem.class)))
                .thenAnswer(i -> i.getArgument(0));

        // Act
        inventoryService.deductStockForOrder(order, adminPrincipal);

        // Assert
        // Order has 2 items, each requiring 0.5kg tomatoes and 0.2kg cheese
        assertEquals(new BigDecimal("99.00"), tomatoes.getCurrentQuantity()); // 100 - (0.5 * 2)
        assertEquals(new BigDecimal("49.60"), cheese.getCurrentQuantity());   // 50 - (0.2 * 2)
        verify(inventoryRepository, times(4)).save(any(InventoryItem.class)); // 2 items * 2 ingredients
    }

    @Test
    void deductStockForOrder_InsufficientStock_ThrowsException() {
        // Arrange
        Order order = createMockOrder();
        InventoryItem tomatoes = createMockInventoryItem(1L, new BigDecimal("0.5")); // Not enough

        MenuItemInventory link = new MenuItemInventory();
        link.setInventoryItem(tomatoes);
        link.setQuantityRequired(new BigDecimal("0.5"));
        link.setIsOptional(false);

        when(menuItemInventoryRepository.findByMenuItemId(1L))
                .thenReturn(List.of(link));

        // Act & Assert
        assertThrows(InsufficientStockException.class,
                () -> inventoryService.deductStockForOrder(order, adminPrincipal));
    }

    @Test
    void checkMenuItemAvailability_Available_ReturnsTrue() {
        // Arrange
        InventoryItem tomatoes = createMockInventoryItem(1L, new BigDecimal("100.00"));

        MenuItemInventory link = new MenuItemInventory();
        link.setInventoryItem(tomatoes);
        link.setQuantityRequired(new BigDecimal("0.5"));
        link.setIsOptional(false);

        when(menuItemInventoryRepository.findByMenuItemId(1L))
                .thenReturn(List.of(link));

        // Act
        StockAvailabilityResponse response = inventoryService
                .checkMenuItemAvailability(1L, 10);

        // Assert
        assertTrue(response.getIsAvailable());
        assertEquals(1, response.getIngredients().size());
        assertTrue(response.getIngredients().get(0).getIsAvailable());
    }

    @Test
    void checkMenuItemAvailability_NotAvailable_ReturnsFalse() {
        // Arrange
        InventoryItem tomatoes = createMockInventoryItem(1L, new BigDecimal("2.00"));

        MenuItemInventory link = new MenuItemInventory();
        link.setInventoryItem(tomatoes);
        link.setQuantityRequired(new BigDecimal("0.5"));
        link.setIsOptional(false);

        when(menuItemInventoryRepository.findByMenuItemId(1L))
                .thenReturn(List.of(link));

        // Act
        StockAvailabilityResponse response = inventoryService
                .checkMenuItemAvailability(1L, 10); // Requires 5kg, only 2kg available

        // Assert
        assertFalse(response.getIsAvailable());
        assertFalse(response.getIngredients().get(0).getIsAvailable());
    }

    @Test
    void linkInventoryToMenuItem_Success() {
        // Arrange
        InventoryItem item = createMockInventoryItem(1L, new BigDecimal("50.00"));
        LinkMenuItemRequest request = new LinkMenuItemRequest();
        request.setMenuItemId(10L);
        request.setQuantityRequired(new BigDecimal("0.5"));
        request.setIsOptional(false);

        when(inventoryRepository.findByIdAndRestaurantId(1L, 100L))
                .thenReturn(Optional.of(item));
        when(menuItemInventoryRepository.existsByMenuItemIdAndInventoryItemId(10L, 1L))
                .thenReturn(false);
        when(menuItemInventoryRepository.save(any(MenuItemInventory.class)))
                .thenAnswer(i -> {
                    MenuItemInventory link = i.getArgument(0);
                    link.setId(1L);
                    return link;
                });

        // Act
        MenuItemInventoryResponse response = inventoryService.linkInventoryToMenuItem(
                1L, request, adminPrincipal);

        // Assert
        assertNotNull(response);
        assertEquals(10L, response.getMenuItemId());
        assertEquals(new BigDecimal("0.5"), response.getQuantityRequired());
        verify(menuItemInventoryRepository).save(any(MenuItemInventory.class));
    }

    @Test
    void linkInventoryToMenuItem_AlreadyLinked_ThrowsBadRequest() {
        // Arrange
        InventoryItem item = createMockInventoryItem(1L, new BigDecimal("50.00"));
        LinkMenuItemRequest request = new LinkMenuItemRequest();
        request.setMenuItemId(10L);
        request.setQuantityRequired(new BigDecimal("0.5"));

        when(inventoryRepository.findByIdAndRestaurantId(1L, 100L))
                .thenReturn(Optional.of(item));
        when(menuItemInventoryRepository.existsByMenuItemIdAndInventoryItemId(10L, 1L))
                .thenReturn(true);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> inventoryService.linkInventoryToMenuItem(1L, request, adminPrincipal));
    }

    // Helper methods
    private UserPrincipal createUserPrincipal(Long userId, Long restaurantId, List<String> roles) {
        return new UserPrincipal(
                userId,
                "user" + userId + "@test.com",
                "password",
                restaurantId,
                roles.stream()
                        .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList()),
                true
        );
    }

    private InventoryItem createMockInventoryItem(Long id, BigDecimal quantity) {
        InventoryItem item = new InventoryItem();
        item.setId(id);
        item.setRestaurantId(100L);
        item.setItemCode("ITEM-" + id);
        item.setName("Test Item " + id);
        item.setCategory(InventoryItem.InventoryCategory.VEGETABLES);
        item.setUnit(InventoryItem.Unit.KG);
        item.setCurrentQuantity(quantity);
        item.setMinimumQuantity(new BigDecimal("10.00"));
        item.setCostPerUnit(new BigDecimal("2.50"));
        item.setStatus(InventoryItem.InventoryStatus.IN_STOCK);
        item.setIsActive(true);
        return item;
    }

    private Order createMockOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setOrderNumber("ORD123");

        OrderItem item1 = new OrderItem();
        item1.setMenuItemId(1L);
        item1.setQuantity(2);
        order.addOrderItem(item1);

        return order;
    }
}
