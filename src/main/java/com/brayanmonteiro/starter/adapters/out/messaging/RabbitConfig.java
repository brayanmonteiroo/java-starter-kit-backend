package com.brayanmonteiro.starter.adapters.out.messaging;

import com.brayanmonteiro.starter.application.auth.ForgotPasswordUseCase;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String MAIL_QUEUE = "starter.mail";

    @Bean
    TopicExchange starterExchange() {
        return new TopicExchange(RabbitDomainEventPublisher.EXCHANGE);
    }

    @Bean
    Queue mailQueue() {
        return new Queue(MAIL_QUEUE, true);
    }

    @Bean
    Binding mailBinding(Queue mailQueue, TopicExchange starterExchange) {
        return BindingBuilder.bind(mailQueue).to(starterExchange).with(ForgotPasswordUseCase.ROUTING_KEY);
    }

    @Bean
    MessageConverter jacksonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
