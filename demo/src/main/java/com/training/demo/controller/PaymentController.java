package com.training.demo.controller;

import com.training.demo.dto.request.Payment.ConfirmPaymentRequest;
import com.training.demo.dto.request.Payment.CreatePaymentRequest;
import com.training.demo.dto.response.System.BaseResponse;
import com.training.demo.security.SecurityUtils;
import com.training.demo.service.PaymentService;
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

@RestController
@RequestMapping("/api/payments")
@Slf4j
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Lấy tất cả payments (ADMIN)
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
     * Tạo payment cho order
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
     * Xác nhận thanh toán (ADMIN)
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
     * Lấy chi tiết payment
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
