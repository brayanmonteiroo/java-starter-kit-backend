package com.brayanmonteiro.starter.adapters.in.messaging;

import com.brayanmonteiro.starter.adapters.out.messaging.RabbitConfig;
import com.brayanmonteiro.starter.domain.notification.MailMessage;
import com.brayanmonteiro.starter.domain.notification.MailPort;
import com.brayanmonteiro.starter.domain.notification.PasswordResetRequestedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetMailListener {
    private final MailPort mailPort;

    public PasswordResetMailListener(MailPort mailPort) {
        this.mailPort = mailPort;
    }

    @RabbitListener(queues = RabbitConfig.MAIL_QUEUE)
    public void onPasswordReset(PasswordResetRequestedEvent event) {
        String body = """
                Olá %s,

                Recebemos um pedido para redefinir sua senha.
                Acesse o link abaixo (válido por 1 hora):

                %s

                Se você não solicitou, ignore este e-mail.
                """.formatted(event.name(), event.resetUrl());
        mailPort.send(new MailMessage(event.email(), "Redefinição de senha", body));
    }
}
