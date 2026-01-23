package dev.safi.restaurant_management_system.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Generic API Response Wrapper
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Generic API response wrapper")
public class ApiResponse<T> {

    @Schema(description = "Success status", example = "true")
    private Boolean success;

    @Schema(description = "Response message", example = "Operation successful")
    private String message;

    @Schema(description = "Response data")
    private T data;

    @Schema(description = "Timestamp")
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}