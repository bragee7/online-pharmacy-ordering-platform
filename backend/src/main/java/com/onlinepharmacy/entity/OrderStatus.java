package com.onlinepharmacy.entity;

public enum OrderStatus {
    PENDING,
    PRESCRIPTION_PENDING,
    CONFIRMED,
    PACKED,
    SHIPPED,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED,
    REJECTED
}