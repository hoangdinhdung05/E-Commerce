package com.training.demo.utils.constants;

public class RabbitMQConstants {

    // Export Module
    public static final String EXPORT_EXCHANGE = "export.exchange";
    public static final String EXPORT_QUEUE = "export.queue";
    public static final String EXPORT_ROUTING_KEY = "export.routing.key";

    // Email Module (cho tương lai)
    public static final String EMAIL_EXCHANGE = "email.exchange";
    public static final String EMAIL_QUEUE = "email.queue";
    public static final String EMAIL_ROUTING_KEY = "email.routing.key";

    // Dead Letter Queue (để xử lý message lỗi)
    public static final String DLX_EXCHANGE = "dlx.exchange";
    public static final String DLQ_QUEUE = "dlq.queue";
    public static final String DLQ_ROUTING_KEY = "dlq.routing.key";

    private RabbitMQConstants() {}
}