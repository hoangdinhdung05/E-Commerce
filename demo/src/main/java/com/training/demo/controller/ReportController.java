package com.training.demo.controller;

import com.training.demo.dto.response.System.BaseResponse;
import com.training.demo.producer.ExportProducer;
import com.training.demo.security.SecurityUtils;
import com.training.demo.service.JasperReportService;
import com.training.demo.utils.enums.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@Slf4j
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
public class ReportController {

    private final JasperReportService reportService;
    private final ExportProducer exportProducer;

    public ReportController(JasperReportService reportService, ExportProducer exportProducer) {
        this.reportService = reportService;
        this.exportProducer = exportProducer;
    }

    /**
     * Generate user report synchronously (old method - kept for backward compatibility)
     */
    @GetMapping("/users")
    public ResponseEntity<byte[]> generateUserReport(@RequestParam(required = false) String username) {
        log.info("[ReportController] Request to generate user report synchronously");

        byte[] pdfBytes = reportService.generateUserReportPdf(username);

        log.info("[ReportController] Returning PDF report users, size: {} bytes", pdfBytes.length);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=users_report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    /**
     * Generate user report asynchronously via RabbitMQ queue
     */
    @GetMapping("/users/async")
    public ResponseEntity<?> generateUserReportAsync(@RequestParam(required = false) String username) {
        log.info("[ReportController] Request to generate user report asynchronously");

        Long userId = SecurityUtils.getCurrentUserId();
        exportProducer.exportUserReport(userId, username);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(BaseResponse.success(
                        "Report generation has been queued. You will receive an email when it's ready."
                ));
    }

    /**
     * Generate product report synchronously (old method - kept for backward compatibility)
     */
    @GetMapping("/products")
    public ResponseEntity<byte[]> exportProductsPdf(@RequestParam(required = false) String name) {
        log.info("[ReportController] Request to generate products report synchronously");

        byte[] pdfBytes = reportService.generateProductReportPdf(name);

        log.info("[ReportController] Returning PDF report products, size: {} bytes", pdfBytes.length);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=products.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    /**
     * Generate product report asynchronously via RabbitMQ queue
     */
    @GetMapping("/products/async")
    public ResponseEntity<?> exportProductsPdfAsync(@RequestParam(required = false) String name) {
        log.info("[ReportController] Request to generate products report asynchronously");

        Long userId = SecurityUtils.getCurrentUserId();
        exportProducer.exportProductReport(userId, name);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(BaseResponse.success(
                        "Report generation has been queued. You will receive an email when it's ready."
                ));
    }

    @GetMapping("/orders/async")
    public ResponseEntity<?> exportOrdersAsync(
            @RequestParam(required = false) String orderNumber,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) OrderStatus status) {
        log.info("[ReportController] Request to generate orders report asynchronously, orderNumber={}, username={}, status={}",
                orderNumber, username, status);

        Long userId = SecurityUtils.getCurrentUserId();

        String fileName = "orders_report_" + System.currentTimeMillis() + ".pdf";
        exportProducer.exportOrderReport(userId, orderNumber, username, status, fileName);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(BaseResponse.success(
                        Map.of(
                                "message", "Order report generation has been queued.",
                                "fileName", fileName
                        )
                ));
    }

    /**
     * Generate payment report asynchronously via RabbitMQ queue
     */
    @GetMapping("/payments/async")
    public ResponseEntity<?> exportPaymentsAsync(@RequestParam(required = false) String status) {
        log.info("[ReportController] Request to generate payments report asynchronously");

        Long userId = SecurityUtils.getCurrentUserId();
        exportProducer.exportPaymentReport(userId, status);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(BaseResponse.success(
                        "Payment report generation has been queued. You will receive an email when it's ready."
                ));
    }

    /**
     * Generate payment report synchronously - Direct download like products
     */
    @GetMapping("/payments")
    public ResponseEntity<byte[]> exportPaymentsPdf(@RequestParam(required = false) String status) {
        log.info("[ReportController] Request to generate payments report synchronously");

        byte[] pdfBytes = reportService.generatePaymentReportPdf(status);

        log.info("[ReportController] Returning PDF report payments, size: {} bytes", pdfBytes.length);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=payments.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
