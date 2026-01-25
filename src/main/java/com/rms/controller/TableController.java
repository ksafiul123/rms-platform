package com.rms.controller;

//package com.rms.controller;

import com.rms.dto.auth.ApiResponse;
import com.rms.dto.TableDTO.*;
import com.rms.entity.Table;
import com.rms.entity.TableSession;
import com.rms.security.CurrentUser;
import com.rms.security.UserPrincipal;
import com.rms.service.TableService;
import com.rms.service.TableSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
public class TableController {

    private final TableService tableService;
    private final TableSessionService sessionService;

    // ========== Table Management Endpoints ==========

    @PostMapping
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<TableResponse>> createTable(
            @Valid @RequestBody CreateTableRequest request,
            @CurrentUser UserPrincipal currentUser) {

        TableResponse table = tableService.createTable(request, currentUser);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Table created successfully", table));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'ADMIN', 'CHEF')")
    public ResponseEntity<ApiResponse<Page<TableResponse>>> getTables(
            @RequestParam(required = false) Table.TableStatus status,
            @PageableDefault(size = 50, sort = "tableNumber", direction = Sort.Direction.ASC)
            Pageable pageable,
            @CurrentUser UserPrincipal currentUser) {

        Page<TableResponse> tables = tableService.getTables(currentUser, status, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Tables retrieved successfully", tables));
    }

    @GetMapping("/{tableId}")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'ADMIN', 'CHEF')")
    public ResponseEntity<ApiResponse<TableResponse>> getTableById(
            @PathVariable Long tableId,
            @CurrentUser UserPrincipal currentUser) {

        TableResponse table = tableService.getTableById(tableId, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Table retrieved successfully", table));
    }

    @PutMapping("/{tableId}")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<TableResponse>> updateTable(
            @PathVariable Long tableId,
            @Valid @RequestBody UpdateTableRequest request,
            @CurrentUser UserPrincipal currentUser) {

        TableResponse table = tableService.updateTable(tableId, request, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Table updated successfully", table));
    }

    @DeleteMapping("/{tableId}")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTable(
            @PathVariable Long tableId,
            @CurrentUser UserPrincipal currentUser) {

        tableService.deleteTable(tableId, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Table deleted successfully", null));
    }

    @PostMapping("/{tableId}/regenerate-qr")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<QRCodeResponse>> regenerateQRCode(
            @PathVariable Long tableId,
            @CurrentUser UserPrincipal currentUser) {

        QRCodeResponse qrCode = tableService.regenerateQRCode(tableId, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("QR code regenerated successfully", qrCode));
    }

    // ========== QR Code Scanning Endpoints (Public) ==========

    @GetMapping("/scan/{qrCode}")
    public ResponseEntity<ApiResponse<ScanQRResponse>> scanQRCode(
            @PathVariable String qrCode) {

        ScanQRResponse response = tableService.scanQRCode(qrCode);

        return ResponseEntity.ok(
                ApiResponse.success("QR code scanned successfully", response));
    }

    // ========== Table Session Endpoints ==========

    @PostMapping("/scan/{qrCode}/start-session")
    public ResponseEntity<ApiResponse<TableSessionResponse>> startSession(
            @PathVariable String qrCode,
            @RequestBody(required = false) StartSessionRequest request,
            @CurrentUser(required = false) UserPrincipal currentUser) {

        if (request == null) {
            request = new StartSessionRequest();
        }

        TableSessionResponse session = sessionService.startSession(
                qrCode, request, currentUser);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Session started successfully", session));
    }

    @PostMapping("/session/join")
    public ResponseEntity<ApiResponse<TableSessionResponse>> joinSession(
            @Valid @RequestBody JoinSessionRequest request,
            @CurrentUser(required = false) UserPrincipal currentUser) {

        TableSessionResponse session = sessionService.joinSession(
                request.getSessionCode(), request, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Joined session successfully", session));
    }

    @PostMapping("/session/{sessionId}/leave")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> leaveSession(
            @PathVariable Long sessionId,
            @CurrentUser UserPrincipal currentUser) {

        sessionService.leaveSession(sessionId, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Left session successfully", null));
    }

    @PostMapping("/session/{sessionId}/end")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TableSessionResponse>> endSession(
            @PathVariable Long sessionId,
            @RequestBody(required = false) EndSessionRequest request,
            @CurrentUser UserPrincipal currentUser) {

        if (request == null) {
            request = new EndSessionRequest();
        }

        TableSessionResponse session = sessionService.endSession(
                sessionId, request, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Session ended successfully", session));
    }

    @GetMapping("/session/{sessionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TableSessionResponse>> getSessionById(
            @PathVariable Long sessionId,
            @CurrentUser UserPrincipal currentUser) {

        TableSessionResponse session = sessionService.getSessionById(
                sessionId, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Session retrieved successfully", session));
    }

    @GetMapping("/session/code/{sessionCode}")
    public ResponseEntity<ApiResponse<TableSessionResponse>> getSessionByCode(
            @PathVariable String sessionCode) {

        TableSessionResponse session = sessionService.getSessionByCode(sessionCode);

        return ResponseEntity.ok(
                ApiResponse.success("Session retrieved successfully", session));
    }

    @GetMapping("/sessions")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<TableSessionResponse>>> getSessions(
            @RequestParam(required = false) TableSession.SessionStatus status,
            @PageableDefault(size = 20, sort = "startedAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            @CurrentUser UserPrincipal currentUser) {

        Page<TableSessionResponse> sessions = sessionService.getSessions(
                currentUser, status, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Sessions retrieved successfully", sessions));
    }

    @GetMapping("/my-sessions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<TableSessionResponse>>> getMyActiveSessions(
            @CurrentUser UserPrincipal currentUser) {

        List<TableSessionResponse> sessions = sessionService.getMyActiveSessions(currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Your active sessions retrieved successfully", sessions));
    }
}
