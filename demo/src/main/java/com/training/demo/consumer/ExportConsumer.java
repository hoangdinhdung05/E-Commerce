package com.training.demo.consumer;

import com.rabbitmq.client.Channel;
import com.training.demo.dto.ExportMessageDTO;
import com.training.demo.dto.response.Order.ExportOrderResponse;
import com.training.demo.dto.response.Payment.ExportPaymentResponse;
import com.training.demo.dto.response.Product.ExportProductResponse;
import com.training.demo.dto.response.User.ExportUserResponse;
import com.training.demo.helpers.Reports.JasperReportGenerator;
import com.training.demo.repository.OrderRepository;
import com.training.demo.repository.PaymentRepository;
import com.training.demo.repository.ProductRepository;
import com.training.demo.repository.UserRepository;
import com.training.demo.utils.constants.RabbitMQConstants;
import com.training.demo.utils.enums.OrderStatus;
import com.training.demo.utils.enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExportConsumer {

    private final JasperReportGenerator jasperReportGenerator;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    private static final String EXPORT_DIR = "exports/";

    @RabbitListener(queues = RabbitMQConstants.EXPORT_QUEUE)
    public void processExportRequest(
            ExportMessageDTO exportMessage,
            Channel channel,
            Message message,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {

        try {
            log.info("[ExportConsumer] Processing export - Type: {}, User: {}, File: {}", 
                    exportMessage.getExportType(),
                    exportMessage.getUserId(),
                    exportMessage.getFileName());

            // Create export directory if not exists
            Path exportPath = Paths.get(EXPORT_DIR);
            if (!Files.exists(exportPath)) {
                Files.createDirectories(exportPath);
            }

            byte[] pdfBytes = null;
            String filePath = EXPORT_DIR + exportMessage.getFileName();

            // Generate report based on type
            switch (exportMessage.getExportType()) {
                case "USER_PDF":
                    pdfBytes = generateUserReport(exportMessage);
                    break;
                case "PRODUCT_PDF":
                    pdfBytes = generateProductReport(exportMessage);
                    break;
                case "ORDER_PDF":
                    pdfBytes = generateOrderReport(exportMessage);
                    break;
                case "PAYMENT_PDF":
                    pdfBytes = generatePaymentReport(exportMessage);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown export type: " + exportMessage.getExportType());
            }

            // Save to file
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(pdfBytes);
            }

            log.info("[ExportConsumer] Export completed - File saved: {}", filePath);

            // Acknowledge success
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("[ExportConsumer] Failed to process export - Type: {}, Error: {}", 
                    exportMessage.getExportType(), e.getMessage(), e);

            // Retry logic - send to DLQ after max retries
            channel.basicNack(deliveryTag, false, false);
        }
    }

    /**
     * Generate user report PDF
     */
    private byte[] generateUserReport(ExportMessageDTO exportMessage) throws JRException {
        String username = (String) exportMessage.getParameters().get("username");
        
        log.info("[ExportConsumer] Generating user report, filter: {}", username);
        List<ExportUserResponse> users = userRepository.findAllWithRoles(username);
        
        JasperPrint jasperPrint = jasperReportGenerator.generateUserReport(users);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
        
        return outputStream.toByteArray();
    }

    /**
     * Generate product report PDF
     */
    private byte[] generateProductReport(ExportMessageDTO exportMessage) throws JRException {
        String productName = (String) exportMessage.getParameters().get("productName");
        
        log.info("[ExportConsumer] Generating product report, filter: {}", productName);
        List<ExportProductResponse> products = productRepository.findAllProjectedWithCategory(productName);
        
        JasperPrint jasperPrint = jasperReportGenerator.generateProductReport(products);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
        
        return outputStream.toByteArray();
    }

    /**
     * Generate order report PDF
     */
    private byte[] generateOrderReport(ExportMessageDTO exportMessage) throws JRException {
        // Lấy map ra, log luôn cho chắc
        Map<String, Object> params = exportMessage.getParameters();
        log.info("[ExportConsumer] Raw parameters: {}", params);

        String orderNumber = (String) params.get("orderNumber");
        String username    = (String) params.get("username");
        Object statusObj   = params.get("status");

        OrderStatus orderStatus = null;
        if (statusObj != null) {
            // Nếu Producer put enum trực tiếp, nó sẽ deserialize thành LinkedHashMap/String tùy config.
            // Để an toàn hơn, nên cho Producer gửi status.name() (String).
            // Tạm thời xử lý kiểu String:
            String statusStr = statusObj.toString();
            if (!statusStr.isBlank()) {
                orderStatus = OrderStatus.valueOf(statusStr);
            }
        }

        log.info("[ExportConsumer] Generating order report, filter: orderNumber={}, username={}, status={}",
                orderNumber, username, orderStatus);

        List<ExportOrderResponse> orders =
                orderRepository.findAllForExport(orderNumber, username, orderStatus);

        JasperPrint jasperPrint = jasperReportGenerator.generateOrderReport(orders);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);

        return outputStream.toByteArray();
    }


    /**
     * Generate payment report PDF
     */
    private byte[] generatePaymentReport(ExportMessageDTO exportMessage) throws JRException {
        String statusStr = (String) exportMessage.getParameters().get("status");
        PaymentStatus status = statusStr != null ? PaymentStatus.valueOf(statusStr) : null;
        
        log.info("[ExportConsumer] Generating payment report, filter: {}", status);
        List<ExportPaymentResponse> payments = paymentRepository.findAllForExport(status);
        
        JasperPrint jasperPrint = jasperReportGenerator.generatePaymentReport(payments);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
        
        return outputStream.toByteArray();
    }


}
