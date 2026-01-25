package com.rms.service;

//package com.rms.service;

import com.rms.dto.order.OrderDTO.*;
import com.rms.entity.Order;
import com.rms.entity.User;
import com.rms.exception.BadRequestException;
import com.rms.exception.ForbiddenException;
import com.rms.exception.ResourceNotFoundException;
import com.rms.repository.OrderRepository;
import com.rms.repository.UserRepository;
import com.rms.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MenuService menuService;

    @InjectMocks
    private OrderService orderService;

    private UserPrincipal customerPrincipal;
    private UserPrincipal adminPrincipal;
    private UserPrincipal chefPrincipal;
    private UserPrincipal deliveryManPrincipal;

    @BeforeEach
    void setUp() {
        customerPrincipal = createUserPrincipal(1L, 100L, List.of("CUSTOMER"));
        adminPrincipal = createUserPrincipal(2L, 100L, List.of("RESTAURANT_ADMIN"));
        chefPrincipal = createUserPrincipal(3L, 100L, List.of("CHEF"));
        deliveryManPrincipal = createUserPrincipal(4L, 100L, List.of("DELIVERY_MAN"));
    }

    @Test
    void createOrder_DineIn_Success() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest();
        request.setOrderType(Order.OrderType.DINE_IN);
        request.setTableNumber("T5");

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setMenuItemId(1L);
        itemRequest.setQuantity(2);
        request.setItems(List.of(itemRequest));

        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order order = i.getArgument(0);
            order.setId(1L);
            return order;
        });

        // Act
        OrderResponse response = orderService.createOrder(request, customerPrincipal);

        // Assert
        assertNotNull(response);
        assertEquals(Order.OrderType.DINE_IN, response.getOrderType());
        assertEquals("T5", response.getTableNumber());
        assertEquals(Order.OrderStatus.PENDING, response.getStatus());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_DineIn_WithoutTableNumber_ThrowsBadRequest() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest();
        request.setOrderType(Order.OrderType.DINE_IN);
        request.setItems(List.of(new OrderItemRequest()));

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> orderService.createOrder(request, customerPrincipal));
    }

    @Test
    void createOrder_Delivery_Success() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest();
        request.setOrderType(Order.OrderType.DELIVERY);
        request.setDeliveryAddress("123 Main St");

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setMenuItemId(1L);
        itemRequest.setQuantity(1);
        request.setItems(List.of(itemRequest));

        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order order = i.getArgument(0);
            order.setId(1L);
            return order;
        });

        // Act
        OrderResponse response = orderService.createOrder(request, customerPrincipal);

        // Assert
        assertNotNull(response);
        assertEquals(Order.OrderType.DELIVERY, response.getOrderType());
        assertEquals("123 Main St", response.getDeliveryAddress());
        assertTrue(response.getDeliveryFee().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void createOrder_Delivery_WithoutAddress_ThrowsBadRequest() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest();
        request.setOrderType(Order.OrderType.DELIVERY);
        request.setItems(List.of(new OrderItemRequest()));

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> orderService.createOrder(request, customerPrincipal));
    }

    @Test
    void createOrder_NonCustomer_ThrowsForbidden() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest();
        request.setOrderType(Order.OrderType.TAKEAWAY);
        request.setItems(List.of(new OrderItemRequest()));

        // Act & Assert
        assertThrows(ForbiddenException.class,
                () -> orderService.createOrder(request, chefPrincipal));
    }

    @Test
    void updateOrderStatus_ValidTransition_Success() {
        // Arrange
        Order order = createMockOrder(1L, Order.OrderStatus.PENDING);
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setStatus(Order.OrderStatus.CONFIRMED);

        when(orderRepository.findByIdAndRestaurantId(1L, 100L))
                .thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        OrderResponse response = orderService.updateOrderStatus(1L, request, adminPrincipal);

        // Assert
        assertNotNull(response);
        verify(orderRepository).save(argThat(o ->
                o.getStatus() == Order.OrderStatus.CONFIRMED
        ));
    }

    @Test
    void updateOrderStatus_InvalidTransition_ThrowsBadRequest() {
        // Arrange
        Order order = createMockOrder(1L, Order.OrderStatus.PENDING);
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setStatus(Order.OrderStatus.COMPLETED);

        when(orderRepository.findByIdAndRestaurantId(1L, 100L))
                .thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> orderService.updateOrderStatus(1L, request, adminPrincipal));
    }

    @Test
    void updateOrderStatus_CancelledOrder_ThrowsBadRequest() {
        // Arrange
        Order order = createMockOrder(1L, Order.OrderStatus.CANCELLED);
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setStatus(Order.OrderStatus.CONFIRMED);

        when(orderRepository.findByIdAndRestaurantId(1L, 100L))
                .thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> orderService.updateOrderStatus(1L, request, adminPrincipal));
    }

    @Test
    void updateOrderStatus_InsufficientPermissions_ThrowsForbidden() {
        // Arrange
        Order order = createMockOrder(1L, Order.OrderStatus.PENDING);
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setStatus(Order.OrderStatus.CONFIRMED);

        when(orderRepository.findByIdAndRestaurantId(1L, 100L))
                .thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(ForbiddenException.class,
                () -> orderService.updateOrderStatus(1L, request, chefPrincipal));
    }

    @Test
    void cancelOrder_CustomerCancelsPending_Success() {
        // Arrange
        Order order = createMockOrder(1L, Order.OrderStatus.PENDING);
        order.setCustomerId(1L);
        CancelOrderRequest request = new CancelOrderRequest();
        request.setReason("Changed my mind");

        when(orderRepository.findByIdAndRestaurantId(1L, 100L))
                .thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        OrderResponse response = orderService.cancelOrder(1L, request, customerPrincipal);

        // Assert
        assertNotNull(response);
        verify(orderRepository).save(argThat(o ->
                o.getStatus() == Order.OrderStatus.CANCELLED &&
                        o.getCancellationReason().equals("Changed my mind")
        ));
    }

    @Test
    void cancelOrder_CustomerCancelsConfirmed_ThrowsForbidden() {
        // Arrange
        Order order = createMockOrder(1L, Order.OrderStatus.CONFIRMED);
        order.setCustomerId(1L);
        CancelOrderRequest request = new CancelOrderRequest();
        request.setReason("Too late");

        when(orderRepository.findByIdAndRestaurantId(1L, 100L))
                .thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(ForbiddenException.class,
                () -> orderService.cancelOrder(1L, request, customerPrincipal));
    }

    @Test
    void cancelOrder_AlreadyCancelled_ThrowsBadRequest() {
        // Arrange
        Order order = createMockOrder(1L, Order.OrderStatus.CANCELLED);
        CancelOrderRequest request = new CancelOrderRequest();
        request.setReason("Duplicate");

        when(orderRepository.findByIdAndRestaurantId(1L, 100L))
                .thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> orderService.cancelOrder(1L, request, adminPrincipal));
    }

    @Test
    void assignDeliveryMan_ValidOrder_Success() {
        // Arrange
        Order order = createMockOrder(1L, Order.OrderStatus.READY);
        order.setOrderType(Order.OrderType.DELIVERY);

        User deliveryMan = new User();
        deliveryMan.setId(4L);

        AssignDeliveryManRequest request = new AssignDeliveryManRequest();
        request.setDeliveryManId(4L);

        when(orderRepository.findByIdAndRestaurantId(1L, 100L))
                .thenReturn(Optional.of(order));
        when(userRepository.findById(4L)).thenReturn(Optional.of(deliveryMan));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        OrderResponse response = orderService.assignDeliveryMan(1L, request, adminPrincipal);

        // Assert
        assertNotNull(response);
        verify(orderRepository).save(argThat(o ->
                o.getDeliveryManId().equals(4L)
        ));
    }

    @Test
    void assignDeliveryMan_NotDeliveryOrder_ThrowsBadRequest() {
        // Arrange
        Order order = createMockOrder(1L, Order.OrderStatus.READY);
        order.setOrderType(Order.OrderType.DINE_IN);

        AssignDeliveryManRequest request = new AssignDeliveryManRequest();
        request.setDeliveryManId(4L);

        when(orderRepository.findByIdAndRestaurantId(1L, 100L))
                .thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> orderService.assignDeliveryMan(1L, request, adminPrincipal));
    }

    @Test
    void getOrders_Customer_OnlySeesOwnOrders() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Order> orders = List.of(createMockOrder(1L, Order.OrderStatus.PENDING));
        Page<Order> orderPage = new PageImpl<>(orders);

        when(orderRepository.findByCustomerIdAndRestaurantId(1L, 100L, pageable))
                .thenReturn(orderPage);

        // Act
        Page<OrderSummaryResponse> result = orderService.getOrders(
                customerPrincipal, null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(orderRepository).findByCustomerIdAndRestaurantId(1L, 100L, pageable);
    }

    @Test
    void getOrders_DeliveryMan_OnlySeesAssignedOrders() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Order> orders = List.of(createMockOrder(1L, Order.OrderStatus.OUT_FOR_DELIVERY));
        Page<Order> orderPage = new PageImpl<>(orders);

        when(orderRepository.findByDeliveryManId(4L, pageable))
                .thenReturn(orderPage);

        // Act
        Page<OrderSummaryResponse> result = orderService.getOrders(
                deliveryManPrincipal, null, null, pageable);

        // Assert
        assertNotNull(result);
        verify(orderRepository).findByDeliveryManId(4L, pageable);
    }

    @Test
    void getOrderById_CustomerAccessesOwnOrder_Success() {
        // Arrange
        Order order = createMockOrder(1L, Order.OrderStatus.PENDING);
        order.setCustomerId(1L);

        when(orderRepository.findByIdAndRestaurantId(1L, 100L))
                .thenReturn(Optional.of(order));

        // Act
        OrderResponse response = orderService.getOrderById(1L, customerPrincipal);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void getOrderById_CustomerAccessesOtherOrder_ThrowsForbidden() {
        // Arrange
        Order order = createMockOrder(1L, Order.OrderStatus.PENDING);
        order.setCustomerId(999L);

        when(orderRepository.findByIdAndRestaurantId(1L, 100L))
                .thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(ForbiddenException.class,
                () -> orderService.getOrderById(1L, customerPrincipal));
    }

    // Helper methods
    private UserPrincipal createUserPrincipal(Long userId, Long restaurantId, List<String> roles) {
        return UserPrincipal.builder()
                .id(userId)
                .email("user" + userId + "@test.com")
                .restaurantId(restaurantId)
                .roles(roles)
                .build();
    }

    private Order createMockOrder(Long id, Order.OrderStatus status) {
        Order order = new Order();
        order.setId(id);
        order.setRestaurantId(100L);
        order.setCustomerId(1L);
        order.setOrderNumber("ORD" + id);
        order.setOrderType(Order.OrderType.DINE_IN);
        order.setStatus(status);
        order.setSubtotal(new BigDecimal("50.00"));
        order.setTaxAmount(new BigDecimal("5.00"));
        order.setDeliveryFee(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setTotalAmount(new BigDecimal("55.00"));
        return order;
    }
}
