package com.training.demo.service.impl;

import com.training.demo.config.VnPayProperties;
import com.training.demo.dto.request.Payment.ConfirmPaymentRequest;
import com.training.demo.dto.response.Payment.PaymentResponse;
import com.training.demo.entity.Payment;
import com.training.demo.exception.BadRequestException;
import com.training.demo.exception.NotFoundException;
import com.training.demo.helpers.VnPayHelper;
import com.training.demo.repository.PaymentRepository;
import com.training.demo.service.PaymentService;
import com.training.demo.service.VnPayService;
import com.training.demo.utils.enums.PaymentStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class VnPayServiceImpl implements VnPayService {

    private final VnPayProperties vnPayProperties;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    /**
     * Tạo URL thanh toán VNPay
     *
     * @param paymentId ID của giao dịch thanh toán
     * @param clientIp  Địa chỉ IP của khách hàng
     * @return URL thanh toán VNPay
     */
    @Override
    public String createPaymentUrl(Long paymentId, String clientIp) {
        log.info("[VnPayService] createPaymentUrl for paymentId: {}, clientIp: {}", paymentId, clientIp);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found with id: " + paymentId));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException("Payment is not in PENDING status");
        }

        long amountVnd = payment.getAmount().multiply(new java.math.BigDecimal("100")).longValue();
        if (amountVnd < 500000) { 
            throw new BadRequestException("Amount must be at least 5,000 VND");
        }
        
        String vnpTxnRef = payment.getTransactionId();
        
        // VNPay datetime format: yyyyMMddHHmmss
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String createDate = LocalDateTime.now().format(fmt);
        String expireDate = LocalDateTime.now().plusMinutes(15).format(fmt);
        
        // Fix IPv6 to IPv4 for VNPay compatibility
        String vnpIpAddr = clientIp;
        if ("0:0:0:0:0:0:0:1".equals(clientIp) || "::1".equals(clientIp)) {
            vnpIpAddr = "127.0.0.1";
        }

        //Map parameters - Use TreeMap to auto-sort by key
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnPayProperties.getTmnCode());
        params.put("vnp_Amount", String.valueOf(amountVnd));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", vnpTxnRef);
        params.put("vnp_OrderInfo", "Thanh toan don hang " + payment.getOrder().getOrderNumber());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", vnPayProperties.getReturnUrl());
        params.put("vnp_IpAddr", vnpIpAddr);
        params.put("vnp_CreateDate", createDate);
        params.put("vnp_ExpireDate", expireDate);

        // ❗ HASH TRÊN CHUỖI ĐÃ ENCODE
        String hashData = VnPayHelper.buildQuery(params, true);
        log.info("[VnPayService] Hash data: {}", hashData);

        String secureHash = VnPayHelper.hmacSHA512(vnPayProperties.getHashSecret(), hashData);
        log.info("[VnPayService] Generated SecureHash: {}", secureHash);

        // URL cũng dùng đúng chuỗi hashData
        String paymentUrl = vnPayProperties.getPayUrl()
                + "?" + hashData
                + "&vnp_SecureHash=" + secureHash;

        log.info("[VnPayService] Generated VNPay URL: {}", paymentUrl);
        return paymentUrl;
    }

    /**
     * Xử lý callback trả về từ VNPay
     *
     * @param params Bản đồ các tham số trả về từ VNPay
     * @return Kết quả xử lý thanh toán
     */
    @Override
    @Transactional
    public PaymentResponse handleReturn(Map<String, String> params) {
        log.info("[VnPayService] Handling VNPay return: {}", params);

        if (!VnPayHelper.validateSignature(params, vnPayProperties.getHashSecret())) {
            throw new BadRequestException("Invalid VNPay signature");
        }

        String vnpTxnRef = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        String transactionStatus = params.get("vnp_TransactionStatus");
        String vnpTransactionNo = params.get("vnp_TransactionNo");

        // Lấy payment từ database để xử lý sau khi vnp trả về
        Payment payment = paymentRepository.findByTransactionId(vnpTxnRef)
                .orElseThrow(() -> new NotFoundException("Payment not found with transactionId: " + vnpTxnRef));

        // Nếu payment đã PAID thì trả về luôn
        if (payment.getStatus() == PaymentStatus.PAID) {
            return paymentService.getPaymentById(payment.getId());
        }

        PaymentStatus targetStatus;
        String errorMessage = null;
        if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
            targetStatus = PaymentStatus.PAID;
        } else {
            targetStatus = PaymentStatus.FAILED;
            errorMessage = "VNPay payment failed. responseCode=" + responseCode +
                    ", transactionStatus=" + transactionStatus;
        }

        ConfirmPaymentRequest confirmReq = new ConfirmPaymentRequest();
        confirmReq.setStatus(targetStatus);
        confirmReq.setTransactionId(vnpTransactionNo);
        confirmReq.setPaymentInfo(params.toString());
        confirmReq.setErrorMessage(errorMessage);

        return paymentService.confirmPayment(payment.getId(), confirmReq);
    }

    /**
     * Xử lý IPN từ VNPay
     *
     * @param vnpParams Bản đồ các tham số IPN từ VNPay
     * @return Kết quả xử lý IPN
     */
    @Override
    @Transactional
    public String handleIpn(Map<String, String> vnpParams) {
        log.info("[VnPayService] Handling VNPay IPN: {}", vnpParams);

        if (!VnPayHelper.validateSignature(vnpParams, vnPayProperties.getHashSecret())) {
            return "{\"RspCode\":\"97\",\"Message\":\"Invalid signature\"}";
        }

        String vnpTxnRef = vnpParams.get("vnp_TxnRef");
        String responseCode = vnpParams.get("vnp_ResponseCode");
        String transactionStatus = vnpParams.get("vnp_TransactionStatus");
        String vnpTransactionNo = vnpParams.get("vnp_TransactionNo");

        Payment payment = paymentRepository.findByTransactionId(vnpTxnRef)
                .orElse(null);

        if (payment == null) {
            return "{\"RspCode\":\"01\",\"Message\":\"Payment not found\"}";
        }

        // idempotent
        if (payment.getStatus() == PaymentStatus.PAID) {
            return "{\"RspCode\":\"00\",\"Message\":\"Payment already confirmed\"}";
        }

        PaymentStatus targetStatus;
        String errorMessage = null;

        if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
            targetStatus = PaymentStatus.PAID;
        } else {
            targetStatus = PaymentStatus.FAILED;
            errorMessage = "VNPay IPN failed. responseCode=" + responseCode +
                    ", transactionStatus=" + transactionStatus;
        }

        ConfirmPaymentRequest confirmReq = new ConfirmPaymentRequest();
        confirmReq.setStatus(targetStatus);
        confirmReq.setTransactionId(vnpTransactionNo);
        confirmReq.setPaymentInfo(vnpParams.toString());
        confirmReq.setErrorMessage(errorMessage);

        paymentService.confirmPayment(payment.getId(), confirmReq);

        return "{\"RspCode\":\"00\",\"Message\":\"OK\"}";
    }
}
