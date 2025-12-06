package com.training.demo.controller;

import com.training.demo.dto.request.Payment.ConfirmPaymentRequest;
import com.training.demo.dto.request.Payment.CreatePaymentRequest;
import com.training.demo.dto.response.Payment.PaymentResponse;
import com.training.demo.dto.response.System.BaseResponse;
import com.training.demo.security.SecurityUtils;
import com.training.demo.service.PaymentService;
import com.training.demo.service.VnPayService;
import com.training.demo.utils.enums.PaymentMethod;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@Slf4j
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final VnPayService vnPayService;


    // ========== VNPay Integration Endpoints ==========/
    /**
     * Tạo VNPay payment cho order
     * FE sau này chỉ cần gọi endpoint này để lấy URL redirect
     */
    @PostMapping("/vnpay/create")
    public ResponseEntity<?> createVnPayPayment(@Valid @RequestBody CreatePaymentRequest request,
                                                jakarta.servlet.http.HttpServletRequest httpRequest) {
        log.info("[PaymentController] Creating VNPay payment for order: {}", request.getOrderId());
        Long userId = SecurityUtils.getCurrentUserId();

        // ép method = VNPAY nếu bạn dùng enum/string
        request.setPaymentMethod(PaymentMethod.valueOf("VNPAY"));

        // 1. Tạo Payment (PENDING) dùng service hiện tại
        var paymentResponse = paymentService.createPayment(userId, request);

        // 2. Gọi VNPayService để build URL
        String clientIp = httpRequest.getRemoteAddr();
        String paymentUrl = vnPayService.createPaymentUrl(paymentResponse.getId(), clientIp);

        // 3. Trả ra cho FE
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("payment", paymentResponse);
        data.put("vnpayUrl", paymentUrl);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.success(data));
    }

    /**
     * Return URL: VNPay redirect về đây sau khi user thanh toán (LOCAL test BE).
     * Sau này có FE thì returnUrl sẽ là FE, FE call 1 endpoint verify khác.
     */
    @GetMapping("/vnpay/callback")
    public ResponseEntity<?> handleVnPayCallback(jakarta.servlet.http.HttpServletRequest request) {
        log.info("[PaymentController] Handling VNPay callback");
        Map<String, String> vnpParams = new java.util.HashMap<>();
        request.getParameterMap().forEach((k, v) -> vnpParams.put(k, v[0]));

        PaymentResponse paymentResponse = vnPayService.handleReturn(vnpParams);

        return ResponseEntity.ok(BaseResponse.success(paymentResponse));
    }

    /**
     * IPN (webhook) từ VNPay
     */
    @GetMapping("/vnpay/ipn")
    public ResponseEntity<String> handleVnPayIpn(HttpServletRequest request) {
        log.info("[PaymentController] Handling VNPay IPN");
        Map<String, String> vnpParams = new java.util.HashMap<>();
        request.getParameterMap().forEach((k, v) -> vnpParams.put(k, v[0]));
        String resp = vnPayService.handleIpn(vnpParams);
        return ResponseEntity.ok(resp);
    }

    // ========== Payment Management Endpoints ==========/
    /**
     * Get all payments (ADMIN)
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("[PaymentController] Getting all payments - page: {}, size: {}", page, size);
        
        Sort sort = sortDirection.equalsIgnoreCase("ASC") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return ResponseEntity.ok(BaseResponse.success(
                paymentService.getAllPayments(pageable)
        ));
    }

    /**
     * Create payment for order
     */
    @PostMapping
    public ResponseEntity<?> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        log.info("[PaymentController] Creating payment for order: {}", request.getOrderId());
        Long userId = SecurityUtils.getCurrentUserId();
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.success(paymentService.createPayment(userId, request)));
    }

    /**
     * Confirm payment (ADMIN)
     */
    @PatchMapping("/{paymentId}/confirm")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> confirmPayment(
            @PathVariable Long paymentId,
            @Valid @RequestBody ConfirmPaymentRequest request) {
        log.info("[PaymentController] Confirming payment: {}", paymentId);
        
        return ResponseEntity.ok(BaseResponse.success(
                paymentService.confirmPayment(paymentId, request)
        ));
    }

    /**
     * Get payment details
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<?> getPaymentById(@PathVariable Long paymentId) {
        log.info("[PaymentController] Getting payment: {}", paymentId);
        
        return ResponseEntity.ok(BaseResponse.success(
                paymentService.getPaymentById(paymentId)
        ));
    }

    /**
     * Lấy lịch sử thanh toán của order
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getOrderPayments(@PathVariable Long orderId) {
        log.info("[PaymentController] Getting payments for order: {}", orderId);
        
        return ResponseEntity.ok(BaseResponse.success(
                paymentService.getOrderPayments(orderId)
        ));
    }

    /**
     * Lấy payment theo transaction ID
     */
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<?> getPaymentByTransaction(@PathVariable String transactionId) {
        log.info("[PaymentController] Getting payment by transaction: {}", transactionId);
        
        return ResponseEntity.ok(BaseResponse.success(
                paymentService.getPaymentByTransactionId(transactionId)
        ));
    }

    /**
     * Hủy payment
     */
    @PatchMapping("/{paymentId}/cancel")
    public ResponseEntity<?> cancelPayment(@PathVariable Long paymentId) {
        log.info("[PaymentController] Canceling payment: {}", paymentId);
        
        return ResponseEntity.ok(BaseResponse.success(
                paymentService.cancelPayment(paymentId)
        ));
    }

    /**
     * Đếm tổng số thanh toán (ADMIN)
     */
    @GetMapping("/admin/count")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> countAllPayments() {
        log.info("[PaymentController] Admin counting all payments");
        
        return ResponseEntity.ok(BaseResponse.success(
                paymentService.countAllPayments()
        ));
    }

    /**
     * Tính tổng doanh thu (ADMIN)
     */
    @GetMapping("/admin/total-revenue")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getTotalRevenue() {
        log.info("[PaymentController] Admin getting total revenue");
        
        return ResponseEntity.ok(BaseResponse.success(
                paymentService.getTotalRevenue()
        ));
    }
}
