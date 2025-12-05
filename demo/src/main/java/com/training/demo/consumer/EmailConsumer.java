package com.training.demo.consumer;

import com.rabbitmq.client.Channel;
import com.training.demo.dto.EmailMessageDTO;
import com.training.demo.utils.constants.RabbitMQConstants;
import com.training.demo.utils.enums.EmailType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailConsumer {

    private final JavaMailSender mailSender;
    private final TemplateEngine mailTemplateEngine;

    private static final int MAX_RETRY_COUNT = 3;

    @RabbitListener(queues = RabbitMQConstants.EMAIL_QUEUE)
    public void processEmailMessage(
            EmailMessageDTO emailMessage,
            Channel channel,
            Message message,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {

        try {
            log.info("[EmailConsumer] Processing email - Type: {}, To: {}, Retry: {}", 
                    emailMessage.getEmailType(), 
                    String.join(",", emailMessage.getTo()),
                    emailMessage.getRetryCount());

            // Process email based on type
            if (emailMessage.getEmailType() == EmailType.SIMPLE) {
                sendSimpleEmail(emailMessage);
            } else {
                sendTemplateEmail(emailMessage);
            }

            // Acknowledge success
            channel.basicAck(deliveryTag, false);
            log.info("[EmailConsumer] Email sent successfully - Type: {}", emailMessage.getEmailType());

        } catch (Exception e) {
            log.error("[EmailConsumer] Failed to send email - Type: {}, Error: {}", 
                    emailMessage.getEmailType(), e.getMessage(), e);

            // Check retry count
            int retryCount = emailMessage.getRetryCount() != null ? emailMessage.getRetryCount() : 0;
            
            if (retryCount < MAX_RETRY_COUNT) {
                // Reject and requeue for retry
                log.warn("[EmailConsumer] Requeuing message for retry ({}/{})", retryCount + 1, MAX_RETRY_COUNT);
                emailMessage.setRetryCount(retryCount + 1);
                channel.basicNack(deliveryTag, false, true);
            } else {
                // Max retries reached, send to DLQ
                log.error("[EmailConsumer] Max retries reached, sending to DLQ");
                channel.basicNack(deliveryTag, false, false);
            }
        }
    }

    /**
     * Gửi email đơn giản (plain text)
     */
    private void sendSimpleEmail(EmailMessageDTO emailMessage) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(emailMessage.getTo());
        message.setSubject(emailMessage.getSubject());
        message.setText(emailMessage.getContent());

        mailSender.send(message);
    }

    /**
     * Gửi email từ template (HTML)
     */
    private void sendTemplateEmail(EmailMessageDTO emailMessage) throws MessagingException {
        // Create Thymeleaf context
        Context context = new Context();
        if (emailMessage.getVariables() != null) {
            emailMessage.getVariables().forEach(context::setVariable);
        }

        // Process template
        String htmlContent = mailTemplateEngine.process(emailMessage.getTemplateName(), context);

        // Create MimeMessage
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setTo(emailMessage.getTo());
        helper.setSubject(emailMessage.getSubject());
        helper.setText(htmlContent, true);

        mailSender.send(mimeMessage);
    }
}
