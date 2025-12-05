package com.training.demo.config;

import com.training.demo.utils.constants.RabbitMQConstants;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailQueueConfig {

    @Bean
    public DirectExchange emailExchange() {
        return new DirectExchange(RabbitMQConstants.EMAIL_EXCHANGE);
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(RabbitMQConstants.EMAIL_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitMQConstants.DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RabbitMQConstants.DLQ_ROUTING_KEY)
                .withArgument("x-message-ttl", 1800000) // TTL 30 phút
                .build();
    }

    @Bean
    public Binding emailBinding() {
        return BindingBuilder
                .bind(emailQueue())
                .to(emailExchange())
                .with(RabbitMQConstants.EMAIL_ROUTING_KEY);
    }
}
