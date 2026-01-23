package dev.safi.restaurant_management_system.exception;

/**
 * Custom Exceptions
 */

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
