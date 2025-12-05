package com.training.demo.dto.response.Payment;

import com.training.demo.utils.enums.PaymentMethod;
import com.training.demo.utils.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private String orderNumber;
    private PaymentMethod paymentMethod;
    private BigDecimal amount;
    private String transactionId;
    private PaymentStatus status;
    private LocalDateTime paymentDate;
    private String paymentInfo;
    private String errorMessage;
    private LocalDateTime createdAt;
}
