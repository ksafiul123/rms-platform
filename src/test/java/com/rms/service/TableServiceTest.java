package com.rms.service;

//package com.rms.service;

import com.rms.dto.TableDTO.*;
import com.rms.entity.Order;
import com.rms.entity.Table;
import com.rms.entity.TableSession;
import com.rms.entity.TableSessionGuest;
import com.rms.exception.BadRequestException;
import com.rms.exception.ResourceNotFoundException;
import com.rms.repository.*;
import com.rms.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TableServiceTest {

    @Mock
    private TableRepository tableRepository;

    @Mock
    private TableSessionRepository sessionRepository;

    @Mock
    private TableSessionGuestRepository guestRepository;

    @Mock
    private QRCodeService qrCodeService;

    @InjectMocks
    private TableService tableService;

    @InjectMocks
    private TableSessionService sessionService;

    private UserPrincipal adminPrincipal;
    private UserPrincipal customerPrincipal;

    @BeforeEach
    void setUp() {
        adminPrincipal = createUserPrincipal(1L, 100L, List.of("RESTAURANT_ADMIN"));
        customerPrincipal = createUserPrincipal(2L, 100L, List.of("CUSTOMER"));
    }

    @Test
    void createTable_Success() {
        // Arrange
        CreateTableRequest request = new CreateTableRequest();
        request.setTableNumber("T1");
        request.setCapacity(4);
        request.setFloor("Ground Floor");
        request.setSection("Window Side");

        when(tableRepository.existsByRestaurantIdAndTableNumber(100L, "T1"))
                .thenReturn(false);
        when(qrCodeService.generateQRCodeIdentifier()).thenReturn("QR-ABC123");
        when(tableRepository.existsByQrCode("QR-ABC123")).thenReturn(false);
        when(qrCodeService.generateQRCodeImageBase64("QR-ABC123"))
                .thenReturn("data:image/png;base64,iVBORw0KG...");
        when(tableRepository.save(any(Table.class))).thenAnswer(i -> {
            Table table = i.getArgument(0);
            table.setId(1L);
            return table;
        });

        // Act
        TableResponse response = tableService.createTable(request, adminPrincipal);

        // Assert
        assertNotNull(response);
        assertEquals("T1", response.getTableNumber());
        assertEquals(4, response.getCapacity());
        assertEquals("QR-ABC123", response.getQrCode());
        assertEquals(Table.TableStatus.AVAILABLE, response.getStatus());
        verify(tableRepository).save(any(Table.class));
    }

    @Test
    void createTable_DuplicateTableNumber_ThrowsBadRequest() {
        // Arrange
        CreateTableRequest request = new CreateTableRequest();
        request.setTableNumber("T1");
        request.setCapacity(4);

        when(tableRepository.existsByRestaurantIdAndTableNumber(100L, "T1"))
                .thenReturn(true);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> tableService.createTable(request, adminPrincipal));
    }

    @Test
    void startSession_NewSession_Success() {
        // Arrange
        Table table = createMockTable(1L);
        StartSessionRequest request = new StartSessionRequest();
        request.setGuestName("John Doe");

        when(tableRepository.findByQrCode("QR-ABC123"))
                .thenReturn(Optional.of(table));
        when(sessionRepository.existsBySessionCode(anyString())).thenReturn(false);
        when(sessionRepository.save(any(TableSession.class))).thenAnswer(i -> {
            TableSession session = i.getArgument(0);
            session.setId(1L);
            return session;
        });
        when(tableRepository.save(any(Table.class))).thenReturn(table);

        // Act
        TableSessionResponse response = sessionService.startSession(
                "QR-ABC123", request, customerPrincipal);

        // Assert
        assertNotNull(response);
        assertEquals(TableSession.SessionStatus.ACTIVE, response.getStatus());
        assertEquals(1, response.getGuestCount());
        assertTrue(response.getGuests().get(0).getIsHost());
        verify(tableRepository).save(argThat(t ->
                t.getStatus() == Table.TableStatus.OCCUPIED
        ));
    }

    @Test
    void startSession_TableAlreadyOccupied_ThrowsBadRequest() {
        // Arrange
        Table table = createMockTable(1L);
        TableSession existingSession = new TableSession();
        existingSession.setStatus(TableSession.SessionStatus.ACTIVE);
        table.addSession(existingSession);

        StartSessionRequest request = new StartSessionRequest();

        when(tableRepository.findByQrCode("QR-ABC123"))
                .thenReturn(Optional.of(table));

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> sessionService.startSession("QR-ABC123", request, customerPrincipal));
    }

    @Test
    void joinSession_Success() {
        // Arrange
        TableSession session = createMockSession(1L);
        JoinSessionRequest request = new JoinSessionRequest();
        request.setSessionCode("SESSION-ABC123");
        request.setGuestName("Jane Doe");

        when(sessionRepository.findBySessionCode("SESSION-ABC123"))
                .thenReturn(Optional.of(session));
        when(guestRepository.existsBySessionIdAndUserId(1L, 2L))
                .thenReturn(false);
        when(guestRepository.countActiveGuestsBySessionId(1L)).thenReturn(1L);
        when(sessionRepository.save(any(TableSession.class))).thenReturn(session);

        // Act
        TableSessionResponse response = sessionService.joinSession(
                "SESSION-ABC123", request, customerPrincipal);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getGuestCount());
        verify(sessionRepository).save(any(TableSession.class));
    }

    @Test
    void joinSession_AlreadyInSession_ThrowsBadRequest() {
        // Arrange
        TableSession session = createMockSession(1L);
        JoinSessionRequest request = new JoinSessionRequest();
        request.setSessionCode("SESSION-ABC123");

        when(sessionRepository.findBySessionCode("SESSION-ABC123"))
                .thenReturn(Optional.of(session));
        when(guestRepository.existsBySessionIdAndUserId(1L, 2L))
                .thenReturn(true);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> sessionService.joinSession("SESSION-ABC123", request, customerPrincipal));
    }

    @Test
    void joinSession_TableAtCapacity_ThrowsBadRequest() {
        // Arrange
        TableSession session = createMockSession(1L);
        JoinSessionRequest request = new JoinSessionRequest();
        request.setSessionCode("SESSION-ABC123");

        when(sessionRepository.findBySessionCode("SESSION-ABC123"))
                .thenReturn(Optional.of(session));
        when(guestRepository.existsBySessionIdAndUserId(1L, 2L))
                .thenReturn(false);
        when(guestRepository.countActiveGuestsBySessionId(1L))
                .thenReturn(4L); // At capacity

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> sessionService.joinSession("SESSION-ABC123", request, customerPrincipal));
    }

    @Test
    void endSession_ByHost_Success() {
        // Arrange
        TableSession session = createMockSession(1L);
        TableSessionGuest host = new TableSessionGuest();
        host.setUserId(2L);
        host.setIsHost(true);
        host.setStatus(TableSessionGuest.GuestStatus.ACTIVE);
        session.addGuest(host);

        EndSessionRequest request = new EndSessionRequest();
        request.setNotes("Thanks for dining!");

        when(sessionRepository.findByIdAndRestaurantId(1L, 100L))
                .thenReturn(Optional.of(session));
        when(sessionRepository.save(any(TableSession.class))).thenReturn(session);
        when(tableRepository.save(any(Table.class))).thenReturn(session.getTable());

        // Act
        TableSessionResponse response = sessionService.endSession(
                1L, request, customerPrincipal);

        // Assert
        assertNotNull(response);
        assertEquals(TableSession.SessionStatus.COMPLETED, response.getStatus());
        assertNotNull(response.getEndedAt());
        verify(tableRepository).save(argThat(t ->
                t.getStatus() == Table.TableStatus.AVAILABLE
        ));
    }

    @Test
    void endSession_WithUnsettledOrders_ThrowsBadRequest() {
        // Arrange
        TableSession session = createMockSession(1L);
        TableSessionGuest host = new TableSessionGuest();
        host.setUserId(2L);
        host.setIsHost(true);
        session.addGuest(host);

        // Add unsettled order
        Order order = new Order();
        order.setStatus(Order.OrderStatus.PREPARING);
        session.addOrder(order);

        EndSessionRequest request = new EndSessionRequest();

        when(sessionRepository.findByIdAndRestaurantId(1L, 100L))
                .thenReturn(Optional.of(session));

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> sessionService.endSession(1L, request, customerPrincipal));
    }

    @Test
    void scanQRCode_ValidQR_ReturnsTableInfo() {
        // Arrange
        Table table = createMockTable(1L);

        when(tableRepository.findByQrCode("QR-ABC123"))
                .thenReturn(Optional.of(table));
        when(qrCodeService.generateTableQRUrl("QR-ABC123"))
                .thenReturn("http://localhost:8080/api/tables/scan/QR-ABC123");

        // Act
        ScanQRResponse response = tableService.scanQRCode("QR-ABC123");

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getTableId());
        assertEquals("T1", response.getTableNumber());
        assertFalse(response.getHasActiveSession());
    }

    @Test
    void scanQRCode_InvalidQR_ThrowsNotFound() {
        // Arrange
        when(tableRepository.findByQrCode("INVALID-QR"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> tableService.scanQRCode("INVALID-QR"));
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

    private Table createMockTable(Long id) {
        Table table = new Table();
        table.setId(id);
        table.setRestaurantId(100L);
        table.setTableNumber("T1");
        table.setQrCode("QR-ABC123");
        table.setCapacity(4);
        table.setStatus(Table.TableStatus.AVAILABLE);
        table.setIsActive(true);
        table.setSessions(new ArrayList<>());
        return table;
    }

    private TableSession createMockSession(Long id) {
        TableSession session = new TableSession();
        session.setId(id);
        session.setTable(createMockTable(1L));
        session.setRestaurantId(100L);
        session.setSessionCode("SESSION-ABC123");
        session.setStatus(TableSession.SessionStatus.ACTIVE);
        session.setStartedAt(LocalDateTime.now());
        session.setGuestCount(1);
        session.setTotalAmount(BigDecimal.ZERO);
        session.setGuests(new ArrayList<>());
        session.setOrders(new ArrayList<>());
        return session;
    }
}
