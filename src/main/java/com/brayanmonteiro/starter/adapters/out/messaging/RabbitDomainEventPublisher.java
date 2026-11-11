package com.brayanmonteiro.starter.adapters.out.messaging;

import com.brayanmonteiro.starter.domain.notification.DomainEventPublisherPort;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitDomainEventPublisher implements DomainEventPublisherPort {
    public static final String EXCHANGE = "starter.events";

    private final RabbitTemplate rabbitTemplate;

    public RabbitDomainEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(String routingKey, Object payload) {
        rabbitTemplate.convertAndSend(EXCHANGE, routingKey, payload);
    }
}
