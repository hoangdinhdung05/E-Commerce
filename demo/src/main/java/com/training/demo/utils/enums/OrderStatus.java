package com.training.demo.utils.enums;

public enum OrderStatus {
    PENDING,        // Chờ xác nhận
    CONFIRMED,      // Đã xác nhận
    PROCESSING,     // Đang xử lý
    SHIPPING,       // Đang giao hàng
    DELIVERED,      // Đã giao hàng
    CANCELLED,      // Đã hủy
    REFUNDED        // Đã hoàn tiền
}
