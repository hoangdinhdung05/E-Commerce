package com.training.demo.config;

import com.training.demo.utils.constants.RabbitMQConstants;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExportQueueConfig {

    @Bean
    public DirectExchange exportExchange() {
        return new DirectExchange(RabbitMQConstants.EXPORT_EXCHANGE);
    }

    @Bean
    public Queue exportQueue() {
        return QueueBuilder.durable(RabbitMQConstants.EXPORT_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitMQConstants.DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RabbitMQConstants.DLQ_ROUTING_KEY)
                .withArgument("x-message-ttl", 3600000) // TTL 1 giờ
                .build();
    }

    @Bean
    public Binding exportBinding() {
        return BindingBuilder
                .bind(exportQueue())
                .to(exportExchange())
                .with(RabbitMQConstants.EXPORT_ROUTING_KEY);
    }
}