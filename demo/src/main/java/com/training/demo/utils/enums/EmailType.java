package com.training.demo.utils.enums;

public enum EmailType {
    SIMPLE,             // Plain text email
    TEMPLATE,           // Template-based email
    ORDER_CONFIRMATION, // Order created
    ORDER_CANCELLED,    // Order cancelled
    ORDER_SHIPPED,      // Order shipped
    ORDER_DELIVERED,    // Order delivered
    PAYMENT_SUCCESS,    // Payment successful
    PAYMENT_FAILED,     // Payment failed
    OTP                 // OTP email
}
