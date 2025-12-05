package com.training.demo.producer;

import com.training.demo.dto.EmailMessageDTO;
import com.training.demo.utils.constants.RabbitMQConstants;
import com.training.demo.utils.enums.EmailType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Gửi email message vào queue
     */
    public void sendEmailMessage(EmailMessageDTO message) {
        try {
            message.setRequestedAt(LocalDateTime.now());
            if (message.getRetryCount() == null) {
                message.setRetryCount(0);
            }

            rabbitTemplate.convertAndSend(
                    RabbitMQConstants.EMAIL_EXCHANGE,
                    RabbitMQConstants.EMAIL_ROUTING_KEY,
                    message
            );

            log.info("[EmailProducer] Email message sent to queue - Type: {}, To: {}", 
                    message.getEmailType(), String.join(",", message.getTo()));
        } catch (Exception e) {
            log.error("[EmailProducer] Failed to send email message to queue", e);
            throw new RuntimeException("Failed to queue email", e);
        }
    }

    /**
     * Gửi email đơn giản (text)
     */
    public void sendSimpleEmail(String to, String subject, String content) {
        EmailMessageDTO message = EmailMessageDTO.builder()
                .to(new String[]{to})
                .subject(subject)
                .content(content)
                .emailType(EmailType.SIMPLE)
                .build();

        sendEmailMessage(message);
    }

    /**
     * Gửi email từ template
     */
    public void sendTemplateEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        EmailMessageDTO message = EmailMessageDTO.builder()
                .to(new String[]{to})
                .subject(subject)
                .templateName(templateName)
                .variables(variables)
                .emailType(EmailType.TEMPLATE)
                .build();

        sendEmailMessage(message);
    }

    /**
     * Gửi email xác nhận đơn hàng
     */
    public void sendOrderConfirmationEmail(String to, Map<String, Object> orderData) {
        EmailMessageDTO message = EmailMessageDTO.builder()
                .to(new String[]{to})
                .subject("Xác nhận đơn hàng - " + orderData.get("orderNumber"))
                .templateName("order-confirmation")
                .variables(orderData)
                .emailType(EmailType.ORDER_CONFIRMATION)
                .build();

        sendEmailMessage(message);
    }

    /**
     * Gửi email thanh toán thành công
     */
    public void sendPaymentSuccessEmail(String to, Map<String, Object> paymentData) {
        EmailMessageDTO message = EmailMessageDTO.builder()
                .to(new String[]{to})
                .subject("Thanh toán thành công - " + paymentData.get("orderNumber"))
                .templateName("payment-success")
                .variables(paymentData)
                .emailType(EmailType.PAYMENT_SUCCESS)
                .build();

        sendEmailMessage(message);
    }

    /**
     * Gửi email hủy đơn hàng
     */
    public void sendOrderCancelledEmail(String to, Map<String, Object> orderData) {
        EmailMessageDTO message = EmailMessageDTO.builder()
                .to(new String[]{to})
                .subject("Đơn hàng đã bị hủy - " + orderData.get("orderNumber"))
                .templateName("order-cancelled")
                .variables(orderData)
                .emailType(EmailType.ORDER_CANCELLED)
                .build();

        sendEmailMessage(message);
    }

    /**
     * Gửi email cập nhật trạng thái đơn hàng
     */
    public void sendOrderStatusUpdateEmail(String to, Map<String, Object> orderData) {
        EmailMessageDTO message = EmailMessageDTO.builder()
                .to(new String[]{to})
                .subject("Cập nhật đơn hàng - " + orderData.get("orderNumber"))
                .templateName("order-status-update")
                .variables(orderData)
                .emailType(EmailType.ORDER_SHIPPED)
                .build();

        sendEmailMessage(message);
    }
}
