package com.training.demo.mapper;

import com.training.demo.dto.response.Payment.PaymentResponse;
import com.training.demo.entity.Payment;

public class PaymentMapper {

    public static PaymentResponse toPaymentResponse(Payment payment) {
        if (payment == null) return null;

        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder() != null ? payment.getOrder().getId() : null)
                .orderNumber(payment.getOrder() != null ? payment.getOrder().getOrderNumber() : null)
                .paymentMethod(payment.getPaymentMethod())
                .amount(payment.getAmount())
                .transactionId(payment.getTransactionId())
                .status(payment.getStatus())
                .paymentDate(payment.getPaymentDate())
                .paymentInfo(payment.getPaymentInfo())
                .errorMessage(payment.getErrorMessage())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
