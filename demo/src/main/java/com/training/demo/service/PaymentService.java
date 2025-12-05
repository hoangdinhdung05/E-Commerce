package com.training.demo.service;

import com.training.demo.dto.request.Payment.ConfirmPaymentRequest;
import com.training.demo.dto.request.Payment.CreatePaymentRequest;
import com.training.demo.dto.response.Payment.PaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface PaymentService {

    /**
     * Lấy tất cả payments (ADMIN)
     */
    Page<PaymentResponse> getAllPayments(Pageable pageable);

    /**
     * Tạo payment mới
     */
    PaymentResponse createPayment(Long userId, CreatePaymentRequest request);

    /**
     * Xác nhận thanh toán (ADMIN hoặc auto từ gateway)
     */
    PaymentResponse confirmPayment(Long paymentId, ConfirmPaymentRequest request);

    /**
     * Lấy chi tiết payment
     */
    PaymentResponse getPaymentById(Long paymentId);

    /**
     * Lấy lịch sử thanh toán của order
     */
    List<PaymentResponse> getOrderPayments(Long orderId);

    /**
     * Lấy payment theo transaction ID
     */
    PaymentResponse getPaymentByTransactionId(String transactionId);

    /**
     * Hủy payment
     */
    PaymentResponse cancelPayment(Long paymentId);

    /**
     * Đếm tổng số thanh toán (ADMIN)
     */
    long countAllPayments();

    /**
     * Tính tổng doanh thu từ các thanh toán thành công (ADMIN)
     */
    java.math.BigDecimal getTotalRevenue();
}
