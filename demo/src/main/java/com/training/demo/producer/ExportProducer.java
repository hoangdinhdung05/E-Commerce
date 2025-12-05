package com.training.demo.producer;

import com.training.demo.dto.ExportMessageDTO;
import com.training.demo.utils.constants.RabbitMQConstants;
import com.training.demo.utils.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExportProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Gửi export request vào queue
     */
    public void sendExportRequest(ExportMessageDTO exportMessage) {
        try {
            exportMessage.setRequestedAt(LocalDateTime.now());

            rabbitTemplate.convertAndSend(
                    RabbitMQConstants.EXPORT_EXCHANGE,
                    RabbitMQConstants.EXPORT_ROUTING_KEY,
                    exportMessage
            );

            log.info("[ExportProducer] Export request sent to queue - Type: {}, User: {}, File: {}", 
                    exportMessage.getExportType(), 
                    exportMessage.getUserId(),
                    exportMessage.getFileName());
        } catch (Exception e) {
            log.error("[ExportProducer] Failed to send export request to queue", e);
            throw new RuntimeException("Failed to queue export request", e);
        }
    }

    /**
     * Export users report
     */
    public void exportUserReport(Long userId, String username) {
        Map<String, Object> params = new HashMap<>();
        params.put("username", username);

        ExportMessageDTO message = ExportMessageDTO.builder()
                .userId(userId)
                .exportType("USER_PDF")
                .fileName("users_report_" + System.currentTimeMillis() + ".pdf")
                .parameters(params)
                .build();

        sendExportRequest(message);
    }

    /**
     * Export products report
     */
    public void exportProductReport(Long userId, String productName) {
        Map<String, Object> params = new HashMap<>();
        params.put("productName", productName);

        ExportMessageDTO message = ExportMessageDTO.builder()
                .userId(userId)
                .exportType("PRODUCT_PDF")
                .fileName("products_report_" + System.currentTimeMillis() + ".pdf")
                .parameters(params)
                .build();

        sendExportRequest(message);
    }

    /**
     * Export orders report
     */
    public void exportOrderReport(Long userId, String orderNumber, String username, OrderStatus status, String fileName) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderNumber", orderNumber);
        params.put("username", username);
        params.put("status", status != null ? status.name() : null); // gửi String

        ExportMessageDTO message = ExportMessageDTO.builder()
                .userId(userId)
                .exportType("ORDER_PDF")
                .fileName(fileName)
                .parameters(params)
                .build();

        sendExportRequest(message);
    }

    /**
     * Export payments report
     */
    public void exportPaymentReport(Long userId, String status) {
        Map<String, Object> params = new HashMap<>();
        params.put("status", status);

        ExportMessageDTO message = ExportMessageDTO.builder()
                .userId(userId)
                .exportType("PAYMENT_PDF")
                .fileName("payments_report_" + System.currentTimeMillis() + ".pdf")
                .parameters(params)
                .build();

        sendExportRequest(message);
    }
}
