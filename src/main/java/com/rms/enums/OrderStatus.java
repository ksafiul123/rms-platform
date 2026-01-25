package com.rms.enums;

public enum OrderStatus {
    CREATED,        // customer/admin created
    CONFIRMED,      // restaurant accepted
    PREPARING,      // chef started
    READY,          // ready for pickup / delivery
    OUT_FOR_DELIVERY,
    COMPLETED,
    CANCELLED
}

