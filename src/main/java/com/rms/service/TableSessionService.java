package com.rms.service;

//package com.rms.service;

import com.rms.dto.TableDTO.*;
import com.rms.entity.*;
import com.rms.exception.BadRequestException;
import com.rms.exception.ForbiddenException;
import com.rms.exception.ResourceNotFoundException;
import com.rms.repository.*;
import com.rms.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TableSessionService {

    private final TableRepository tableRepository;
    private final TableSessionRepository sessionRepository;
    private final TableSessionGuestRepository guestRepository;
    private final UserRepository userRepository;
    private final QRCodeService qrCodeService;

    @Transactional
    public TableSessionResponse startSession(String qrCode, StartSessionRequest request,
                                             UserPrincipal currentUser) {
        log.info("Starting session for QR code {} by user {}", qrCode,
                currentUser != null ? currentUser.getId() : "anonymous");

        Table table = tableRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid QR code"));

        if (!table.getIsActive()) {
            throw new BadRequestException("This table is not active");
        }

        // Check if table already has active session
        if (table.hasActiveSession()) {
            throw new BadRequestException("Table already has an active session. Join the existing session.");
        }

        // Create new session
        TableSession session = new TableSession();
        session.setTable(table);
        session.setRestaurantId(table.getRestaurantId());
        session.setSessionCode(generateSessionCode());
        session.setStatus(TableSession.SessionStatus.ACTIVE);
        session.setStartedAt(LocalDateTime.now());

        // Add first guest (host)
        TableSessionGuest host = new TableSessionGuest();
        if (currentUser != null) {
            host.setUserId(currentUser.getId());
            String guestName = userRepository.findById(currentUser.getId())
                    .map(User::getFullName)
                    .orElse("Guest");
            host.setGuestName(guestName);
        } else {
            host.setGuestName(request.getGuestName() != null ?
                    request.getGuestName() : "Anonymous Guest");
        }
        host.setIsHost(true);
        host.setStatus(TableSessionGuest.GuestStatus.ACTIVE);
        session.addGuest(host);

        // Update table status
        table.setStatus(Table.TableStatus.OCCUPIED);
        tableRepository.save(table);

        TableSession savedSession = sessionRepository.save(session);
        log.info("Session {} started for table {}",
                savedSession.getSessionCode(), table.getTableNumber());

        return mapToSessionResponse(savedSession);
    }

    @Transactional
    public TableSessionResponse joinSession(String sessionCode, JoinSessionRequest request,
                                            UserPrincipal currentUser) {
        log.info("User {} joining session {}",
                currentUser != null ? currentUser.getId() : "anonymous", sessionCode);

        TableSession session = sessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid session code"));

        if (session.getStatus() != TableSession.SessionStatus.ACTIVE) {
            throw new BadRequestException("Session is not active");
        }

        // Check if user is already in session
        if (currentUser != null &&
                guestRepository.existsBySessionIdAndUserId(session.getId(), currentUser.getId())) {
            throw new BadRequestException("You are already in this session");
        }

        // Check capacity
        Long activeGuests = guestRepository.countActiveGuestsBySessionId(session.getId());
        if (activeGuests >= session.getTable().getCapacity()) {
            throw new BadRequestException("Table is at full capacity");
        }

        // Add guest to session
        TableSessionGuest guest = new TableSessionGuest();
        if (currentUser != null) {
            guest.setUserId(currentUser.getId());
            String guestName = userRepository.findById(currentUser.getId())
                    .map(User::getFullName)
                    .orElse("Guest");
            guest.setGuestName(guestName);
        } else {
            guest.setGuestName(request.getGuestName() != null ?
                    request.getGuestName() : "Anonymous Guest");
        }
        guest.setIsHost(false);
        guest.setStatus(TableSessionGuest.GuestStatus.ACTIVE);
        session.addGuest(guest);

        sessionRepository.save(session);
        log.info("Guest {} joined session {}", guest.getGuestName(), sessionCode);

        return mapToSessionResponse(session);
    }

    @Transactional
    public void leaveSession(Long sessionId, UserPrincipal currentUser) {
        log.info("User {} leaving session {}", currentUser.getId(), sessionId);

        TableSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        TableSessionGuest guest = guestRepository.findBySessionIdAndUserId(
                        sessionId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("You are not in this session"));

        if (guest.getStatus() == TableSessionGuest.GuestStatus.LEFT) {
            throw new BadRequestException("You have already left this session");
        }

        // Mark guest as left
        guest.setStatus(TableSessionGuest.GuestStatus.LEFT);
        guest.setLeftAt(LocalDateTime.now());
        guestRepository.save(guest);

        // If host is leaving and there are other active guests, assign new host
        if (guest.getIsHost()) {
            List<TableSessionGuest> activeGuests = guestRepository
                    .findBySessionIdAndStatus(sessionId, TableSessionGuest.GuestStatus.ACTIVE);

            if (!activeGuests.isEmpty()) {
                TableSessionGuest newHost = activeGuests.get(0);
                newHost.setIsHost(true);
                guestRepository.save(newHost);
                log.info("New host assigned: {}", newHost.getGuestName());
            }
        }

        // Update session guest count
        session.setGuestCount(guestRepository
                .countActiveGuestsBySessionId(sessionId).intValue());
        sessionRepository.save(session);

        log.info("User {} left session {}", currentUser.getId(), sessionId);
    }

    @Transactional
    public TableSessionResponse endSession(Long sessionId, EndSessionRequest request,
                                           UserPrincipal currentUser) {
        log.info("Ending session {} by user {}", sessionId, currentUser.getId());

        TableSession session = sessionRepository.findByIdAndRestaurantId(
                        sessionId, currentUser.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (session.getStatus() != TableSession.SessionStatus.ACTIVE) {
            throw new BadRequestException("Session is not active");
        }

        // Check if user is authorized to end session
        // Host or restaurant staff can end session
        boolean isHost = session.getGuests().stream()
                .anyMatch(g -> g.getUserId() != null &&
                        g.getUserId().equals(currentUser.getId()) &&
                        g.getIsHost());

        boolean isStaff = currentUser.hasAnyRole("RESTAURANT_ADMIN", "ADMIN");

        if (!isHost && !isStaff) {
            throw new ForbiddenException("Only the host or restaurant staff can end the session");
        }

        // Check if all orders are settled
        boolean hasUnsettledOrders = session.getOrders().stream()
                .anyMatch(order -> order.getStatus() != Order.OrderStatus.COMPLETED &&
                        order.getStatus() != Order.OrderStatus.CANCELLED);

        if (hasUnsettledOrders) {
            throw new BadRequestException(
                    "Cannot end session with unsettled orders. Complete or cancel all orders first.");
        }

        // End session
        session.setStatus(TableSession.SessionStatus.COMPLETED);
        session.setEndedAt(LocalDateTime.now());
        session.setNotes(request.getNotes());

        // Mark all active guests as left
        session.getGuests().stream()
                .filter(g -> g.getStatus() == TableSessionGuest.GuestStatus.ACTIVE)
                .forEach(g -> {
                    g.setStatus(TableSessionGuest.GuestStatus.LEFT);
                    g.setLeftAt(LocalDateTime.now());
                });

        // Update table status
        Table table = session.getTable();
        table.setStatus(Table.TableStatus.AVAILABLE);
        tableRepository.save(table);

        TableSession endedSession = sessionRepository.save(session);
        log.info("Session {} ended successfully", sessionId);

        return mapToSessionResponse(endedSession);
    }

    @Transactional(readOnly = true)
    public TableSessionResponse getSessionById(Long sessionId, UserPrincipal currentUser) {
        TableSession session;

        if (currentUser.hasAnyRole("RESTAURANT_ADMIN", "ADMIN")) {
            // Restaurant staff can view any session
            session = sessionRepository.findByIdAndRestaurantId(
                            sessionId, currentUser.getRestaurantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        } else {
            // Customers can only view sessions they're part of
            session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

            boolean isGuest = session.getGuests().stream()
                    .anyMatch(g -> g.getUserId() != null &&
                            g.getUserId().equals(currentUser.getId()));

            if (!isGuest) {
                throw new ForbiddenException("Access denied to this session");
            }
        }

        return mapToSessionResponse(session);
    }

    @Transactional(readOnly = true)
    public TableSessionResponse getSessionByCode(String sessionCode) {
        TableSession session = sessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        return mapToSessionResponse(session);
    }

    @Transactional(readOnly = true)
    public Page<TableSessionResponse> getSessions(UserPrincipal currentUser,
                                                  TableSession.SessionStatus status,
                                                  Pageable pageable) {
        Page<TableSession> sessions;

        if (status != null) {
            sessions = sessionRepository.findByRestaurantIdAndStatus(
                    currentUser.getRestaurantId(), status, pageable);
        } else {
            sessions = sessionRepository.findByRestaurantId(
                    currentUser.getRestaurantId(), pageable);
        }

        return sessions.map(this::mapToSessionResponse);
    }

    @Transactional(readOnly = true)
    public List<TableSessionResponse> getMyActiveSessions(UserPrincipal currentUser) {
        List<TableSession> sessions = sessionRepository
                .findActiveSessionsByUserId(currentUser.getId());

        return sessions.stream()
                .map(this::mapToSessionResponse)
                .collect(Collectors.toList());
    }

    private String generateSessionCode() {
        String code;
        do {
            code = "SESSION-" + UUID.randomUUID().toString()
                    .replace("-", "").substring(0, 12).toUpperCase();
        } while (sessionRepository.existsBySessionCode(code));
        return code;
    }

    private TableSessionResponse mapToSessionResponse(TableSession session) {
        TableSessionResponse response = new TableSessionResponse();
        response.setId(session.getId());
        response.setTableId(session.getTable().getId());
        response.setTableNumber(session.getTable().getTableNumber());
        response.setRestaurantId(session.getRestaurantId());
        response.setSessionCode(session.getSessionCode());
        response.setStatus(session.getStatus());
        response.setStartedAt(session.getStartedAt());
        response.setEndedAt(session.getEndedAt());
        response.setGuestCount(session.getGuestCount());
        response.setTotalAmount(session.getTotalAmount());
        response.setNotes(session.getNotes());
        response.setCreatedAt(session.getCreatedAt());

        response.setGuests(session.getGuests().stream()
                .map(this::mapToGuestResponse)
                .collect(Collectors.toList()));

        response.setOrders(session.getOrders().stream()
                .map(this::mapToOrderSummary)
                .collect(Collectors.toList()));

        return response;
    }

    private SessionGuestResponse mapToGuestResponse(TableSessionGuest guest) {
        SessionGuestResponse response = new SessionGuestResponse();
        response.setId(guest.getId());
        response.setUserId(guest.getUserId());
        response.setGuestName(guest.getGuestName());
        response.setIsHost(guest.getIsHost());
        response.setStatus(guest.getStatus());
        response.setJoinedAt(guest.getJoinedAt());
        response.setLeftAt(guest.getLeftAt());
        return response;
    }

    private OrderSummaryResponse mapToOrderSummary(Order order) {
        OrderSummaryResponse response = new OrderSummaryResponse();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setCustomerId(order.getCustomerId());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());
        return response;
    }
}
