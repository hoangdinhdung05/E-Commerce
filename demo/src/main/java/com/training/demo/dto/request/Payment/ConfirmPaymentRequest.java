package com.training.demo.dto.request.Payment;

import com.training.demo.utils.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmPaymentRequest {

    @NotNull(message = "Payment status is required")
    private PaymentStatus status; // PAID, FAILED, REFUNDED

    private String transactionId; // Transaction ID từ gateway (VNPay, MoMo)
    
    private String paymentInfo; // Thông tin bổ sung từ gateway
    
    private String errorMessage; // Thông tin lỗi nếu thanh toán thất bại
}
