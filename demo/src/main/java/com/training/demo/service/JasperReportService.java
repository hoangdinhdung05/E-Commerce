package com.training.demo.service;

import com.training.demo.dto.response.Payment.ExportPaymentResponse;
import com.training.demo.dto.response.Product.ExportProductResponse;
import com.training.demo.dto.response.User.ExportUserResponse;
import com.training.demo.helpers.Reports.JasperReportGenerator;
import com.training.demo.repository.PaymentRepository;
import com.training.demo.repository.ProductRepository;
import com.training.demo.repository.UserRepository;
import com.training.demo.utils.enums.PaymentStatus;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@Slf4j
public class JasperReportService {

    private final JasperReportGenerator reportGenerator;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;

    public JasperReportService(
            JasperReportGenerator reportGenerator,
            UserRepository userRepository,
            ProductRepository productRepository,
            PaymentRepository paymentRepository) {
        this.reportGenerator = reportGenerator;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.paymentRepository = paymentRepository;
    }

    public byte[] generateUserReportPdf(String username) {
        try {
            log.info("[JasperReportService] Fetching users from database");
            List<ExportUserResponse> users = userRepository.findAllWithRoles(username);
            log.info("[JasperReportService] {} users fetched", users.size());

            log.info("[JasperReportService] Start generating Jasper user report");
            JasperPrint jasperPrint = reportGenerator.generateUserReport(users);
            log.info("[JasperReportService] User report generated successfully");

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
            log.info("[JasperReportService] User report exported to PDF stream");

            return outputStream.toByteArray();
        } catch (JRException e) {
            log.error("[JasperReportService] Error generating user report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate user report", e);
        }
    }

    public byte[] generateProductReportPdf(String name) {
        try {
            log.info("[JasperReportService] Fetching products from database");
            List<ExportProductResponse> products = productRepository.findAllProjectedWithCategory(name);
            log.info("[JasperReportService] {} products fetched", products.size());

            log.info("[JasperReportService] Start generating Jasper product report");
            JasperPrint jasperPrint = reportGenerator.generateProductReport(products);
            log.info("[JasperReportService] Product report generated successfully");

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
            log.info("[JasperReportService] Product report exported to PDF stream");

            return outputStream.toByteArray();
        } catch (JRException e) {
            log.error("[JasperReportService] Error generating product report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate product report", e);
        }
    }

    public byte[] generatePaymentReportPdf(String status) {
        try {
            PaymentStatus paymentStatus = status != null ? PaymentStatus.valueOf(status) : null;
            
            log.info("[JasperReportService] Fetching payments from database");
            List<ExportPaymentResponse> payments = paymentRepository.findAllForExport(paymentStatus);
            log.info("[JasperReportService] {} payments fetched", payments.size());

            log.info("[JasperReportService] Start generating Jasper payment report");
            JasperPrint jasperPrint = reportGenerator.generatePaymentReport(payments);
            log.info("[JasperReportService] Payment report generated successfully");

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
            log.info("[JasperReportService] Payment report exported to PDF stream");

            return outputStream.toByteArray();
        } catch (JRException e) {
            log.error("[JasperReportService] Error generating payment report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate payment report", e);
        }
    }
}