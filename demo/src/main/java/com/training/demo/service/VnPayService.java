package com.training.demo.service;

import com.training.demo.dto.response.Payment.PaymentResponse;
import java.util.Map;

public interface VnPayService {

    /**
     * Tạo URL thanh toán VNPay
     *
     * @param paymentId ID của giao dịch thanh toán
     * @param clientIp  Địa chỉ IP của khách hàng
     * @return URL thanh toán VNPay
     */
    String createPaymentUrl(Long paymentId, String clientIp);

    /**
     * Xử lý callback trả về từ VNPay
     *
     * @param params Bản đồ các tham số trả về từ VNPay
     * @return Kết quả xử lý thanh toán
     */
    PaymentResponse handleReturn(Map<String, String> params);

    /**
     * Xử lý IPN từ VNPay
     *
     * @param params Bản đồ các tham số IPN từ VNPay
     * @return Kết quả xử lý IPN
     */
    String handleIpn(Map<String, String> params);
}
