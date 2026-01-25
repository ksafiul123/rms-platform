package com.rms.service;

//package com.rms.service;

import com.rms.dto.TableDTO.*;
import com.rms.entity.Table;
import com.rms.entity.TableSession;
import com.rms.entity.TableSessionGuest;
import com.rms.exception.BadRequestException;
import com.rms.exception.ResourceNotFoundException;
import com.rms.repository.TableRepository;
import com.rms.repository.TableSessionGuestRepository;
import com.rms.repository.TableSessionRepository;
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
public class TableService {

    private final TableRepository tableRepository;
    private final TableSessionRepository sessionRepository;
    private final TableSessionGuestRepository guestRepository;
    private final QRCodeService qrCodeService;

    @Transactional
    public TableResponse createTable(CreateTableRequest request, UserPrincipal currentUser) {
        log.info("Creating table {} for restaurant {}",
                request.getTableNumber(), currentUser.getRestaurantId());

        // Check if table number already exists
        if (tableRepository.existsByRestaurantIdAndTableNumber(
                currentUser.getRestaurantId(), request.getTableNumber())) {
            throw new BadRequestException("Table number already exists");
        }

        Table table = new Table();
        table.setRestaurantId(currentUser.getRestaurantId());
        table.setBranchId(request.getBranchId());
        table.setTableNumber(request.getTableNumber());
        table.setCapacity(request.getCapacity());
        table.setFloor(request.getFloor());
        table.setSection(request.getSection());
        table.setDescription(request.getDescription());
        table.setStatus(Table.TableStatus.AVAILABLE);
        table.setIsActive(true);

        // Generate unique QR code
        String qrCode;
        do {
            qrCode = qrCodeService.generateQRCodeIdentifier();
        } while (tableRepository.existsByQrCode(qrCode));

        table.setQrCode(qrCode);

        // Generate QR code image
        String qrCodeImage = qrCodeService.generateQRCodeImageBase64(qrCode);
        table.setQrCodeImageUrl(qrCodeImage);

        Table savedTable = tableRepository.save(table);
        log.info("Table {} created successfully with QR code {}",
                savedTable.getTableNumber(), savedTable.getQrCode());

        return mapToTableResponse(savedTable);
    }

    @Transactional
    public TableResponse updateTable(Long tableId, UpdateTableRequest request,
                                     UserPrincipal currentUser) {
        log.info("Updating table {}", tableId);

        Table table = findTableByIdAndRestaurantId(tableId, currentUser.getRestaurantId());

        if (request.getTableNumber() != null &&
                !request.getTableNumber().equals(table.getTableNumber())) {
            if (tableRepository.existsByRestaurantIdAndTableNumber(
                    currentUser.getRestaurantId(), request.getTableNumber())) {
                throw new BadRequestException("Table number already exists");
            }
            table.setTableNumber(request.getTableNumber());
        }

        if (request.getCapacity() != null) {
            table.setCapacity(request.getCapacity());
        }
        if (request.getFloor() != null) {
            table.setFloor(request.getFloor());
        }
        if (request.getSection() != null) {
            table.setSection(request.getSection());
        }
        if (request.getDescription() != null) {
            table.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            // Validate status change
            if (table.hasActiveSession() &&
                    request.getStatus() == Table.TableStatus.AVAILABLE) {
                throw new BadRequestException(
                        "Cannot set table to AVAILABLE while it has an active session");
            }
            table.setStatus(request.getStatus());
        }
        if (request.getIsActive() != null) {
            table.setIsActive(request.getIsActive());
        }

        Table updatedTable = tableRepository.save(table);
        log.info("Table {} updated successfully", tableId);

        return mapToTableResponse(updatedTable);
    }

    @Transactional
    public void deleteTable(Long tableId, UserPrincipal currentUser) {
        log.info("Deleting table {}", tableId);

        Table table = findTableByIdAndRestaurantId(tableId, currentUser.getRestaurantId());

        if (table.hasActiveSession()) {
            throw new BadRequestException("Cannot delete table with active session");
        }

        tableRepository.delete(table);
        log.info("Table {} deleted successfully", tableId);
    }

    @Transactional
    public QRCodeResponse regenerateQRCode(Long tableId, UserPrincipal currentUser) {
        log.info("Regenerating QR code for table {}", tableId);

        Table table = findTableByIdAndRestaurantId(tableId, currentUser.getRestaurantId());

        // Generate new QR code
        String qrCode;
        do {
            qrCode = qrCodeService.generateQRCodeIdentifier();
        } while (tableRepository.existsByQrCode(qrCode));

        table.setQrCode(qrCode);
        String qrCodeImage = qrCodeService.generateQRCodeImageBase64(qrCode);
        table.setQrCodeImageUrl(qrCodeImage);

        tableRepository.save(table);
        log.info("QR code regenerated for table {}", tableId);

        return new QRCodeResponse(
                qrCode,
                qrCodeImage,
                table.getTableNumber(),
                qrCodeService.generateTableQRUrl(qrCode)
        );
    }

    @Transactional(readOnly = true)
    public TableResponse getTableById(Long tableId, UserPrincipal currentUser) {
        Table table = findTableByIdAndRestaurantId(tableId, currentUser.getRestaurantId());
        return mapToTableResponse(table);
    }

    @Transactional(readOnly = true)
    public Page<TableResponse> getTables(UserPrincipal currentUser,
                                         Table.TableStatus status,
                                         Pageable pageable) {
        Page<Table> tables;

        if (status != null) {
            tables = tableRepository.findByRestaurantIdAndStatus(
                    currentUser.getRestaurantId(), status, pageable);
        } else {
            tables = tableRepository.findByRestaurantId(
                    currentUser.getRestaurantId(), pageable);
        }

        return tables.map(this::mapToTableResponse);
    }

    @Transactional(readOnly = true)
    public ScanQRResponse scanQRCode(String qrCode) {
        log.info("QR code scanned: {}", qrCode);

        Table table = tableRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid QR code"));

        if (!table.getIsActive()) {
            throw new BadRequestException("This table is not active");
        }

        ScanQRResponse response = new ScanQRResponse();
        response.setTableId(table.getId());
        response.setTableNumber(table.getTableNumber());
        response.setCapacity(table.getCapacity());
        response.setScanUrl(qrCodeService.generateTableQRUrl(qrCode));

        TableSession activeSession = table.getActiveSession();
        response.setHasActiveSession(activeSession != null);

        if (activeSession != null) {
            response.setActiveSession(mapToSessionSummary(activeSession));
        }

        return response;
    }

    private Table findTableByIdAndRestaurantId(Long tableId, Long restaurantId) {
        return tableRepository.findByIdAndRestaurantId(tableId, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found"));
    }

    private TableResponse mapToTableResponse(Table table) {
        TableResponse response = new TableResponse();
        response.setId(table.getId());
        response.setRestaurantId(table.getRestaurantId());
        response.setBranchId(table.getBranchId());
        response.setTableNumber(table.getTableNumber());
        response.setQrCode(table.getQrCode());
        response.setQrCodeImageUrl(table.getQrCodeImageUrl());
        response.setCapacity(table.getCapacity());
        response.setFloor(table.getFloor());
        response.setSection(table.getSection());
        response.setStatus(table.getStatus());
        response.setIsActive(table.getIsActive());
        response.setDescription(table.getDescription());
        response.setCreatedAt(table.getCreatedAt());
        response.setUpdatedAt(table.getUpdatedAt());

        TableSession activeSession = table.getActiveSession();
        if (activeSession != null) {
            response.setActiveSession(mapToSessionSummary(activeSession));
        }

        return response;
    }

    private TableSessionSummary mapToSessionSummary(TableSession session) {
        TableSessionSummary summary = new TableSessionSummary();
        summary.setId(session.getId());
        summary.setSessionCode(session.getSessionCode());
        summary.setStatus(session.getStatus());
        summary.setGuestCount(session.getGuestCount());
        summary.setTotalAmount(session.getTotalAmount());
        summary.setStartedAt(session.getStartedAt());
        return summary;
    }
}
