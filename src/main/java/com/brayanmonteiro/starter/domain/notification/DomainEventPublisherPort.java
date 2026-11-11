package com.brayanmonteiro.starter.domain.notification;

public interface DomainEventPublisherPort {
    void publish(String routingKey, Object payload);
}
