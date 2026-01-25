package com.rms.dto;
//package com.rms.dto;

import com.rms.entity.Table;
import com.rms.entity.TableSession;
import com.rms.entity.TableSessionGuest;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TableDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateTableRequest {

        @NotBlank(message = "Table number is required")
        @Size(max = 20, message = "Table number cannot exceed 20 characters")
        private String tableNumber;

        @NotNull(message = "Capacity is required")
        @Min(value = 1, message = "Capacity must be at least 1")
        @Max(value = 50, message = "Capacity cannot exceed 50")
        private Integer capacity;

        @Size(max = 50, message = "Floor cannot exceed 50 characters")
        private String floor;

        @Size(max = 50, message = "Section cannot exceed 50 characters")
        private String section;

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        private String description;

        private Long branchId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateTableRequest {

        @Size(max = 20, message = "Table number cannot exceed 20 characters")
        private String tableNumber;

        @Min(value = 1, message = "Capacity must be at least 1")
        @Max(value = 50, message = "Capacity cannot exceed 50")
        private Integer capacity;

        @Size(max = 50, message = "Floor cannot exceed 50 characters")
        private String floor;

        @Size(max = 50, message = "Section cannot exceed 50 characters")
        private String section;

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        private String description;

        private Table.TableStatus status;

        private Boolean isActive;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TableResponse {

        private Long id;
        private Long restaurantId;
        private Long branchId;
        private String tableNumber;
        private String qrCode;
        private String qrCodeImageUrl;
        private Integer capacity;
        private String floor;
        private String section;
        private Table.TableStatus status;
        private Boolean isActive;
        private String description;
        private TableSessionSummary activeSession;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StartSessionRequest {

        @Size(max = 100, message = "Guest name cannot exceed 100 characters")
        private String guestName; // For anonymous guests
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JoinSessionRequest {

        @NotBlank(message = "Session code is required")
        private String sessionCode;

        @Size(max = 100, message = "Guest name cannot exceed 100 characters")
        private String guestName; // For anonymous guests
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EndSessionRequest {

        @Size(max = 500, message = "Notes cannot exceed 500 characters")
        private String notes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TableSessionResponse {

        private Long id;
        private Long tableId;
        private String tableNumber;
        private Long restaurantId;
        private String sessionCode;
        private TableSession.SessionStatus status;
        private LocalDateTime startedAt;
        private LocalDateTime endedAt;
        private Integer guestCount;
        private BigDecimal totalAmount;
        private String notes;
        private List<SessionGuestResponse> guests;
        private List<OrderSummaryResponse> orders;
        private LocalDateTime createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TableSessionSummary {

        private Long id;
        private String sessionCode;
        private TableSession.SessionStatus status;
        private Integer guestCount;
        private BigDecimal totalAmount;
        private LocalDateTime startedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionGuestResponse {

        private Long id;
        private Long userId;
        private String guestName;
        private Boolean isHost;
        private TableSessionGuest.GuestStatus status;
        private LocalDateTime joinedAt;
        private LocalDateTime leftAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QRCodeResponse {

        private String qrCode;
        private String qrCodeImageUrl;
        private String tableNumber;
        private String scanUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScanQRResponse {

        private Long tableId;
        private String tableNumber;
        private Integer capacity;
        private String restaurantName;
        private TableSessionSummary activeSession;
        private Boolean hasActiveSession;
        private String scanUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderSummaryResponse {

        private Long id;
        private String orderNumber;
        private Long customerId;
        private String customerName;
        private BigDecimal totalAmount;
        private com.rms.entity.Order.OrderStatus status;
        private LocalDateTime createdAt;
    }
}
