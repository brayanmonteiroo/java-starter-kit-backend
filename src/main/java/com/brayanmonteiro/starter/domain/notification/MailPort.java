package com.brayanmonteiro.starter.domain.notification;

public interface MailPort {
    void send(MailMessage message);
}
