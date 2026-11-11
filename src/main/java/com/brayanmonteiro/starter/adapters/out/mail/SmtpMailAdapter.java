package com.brayanmonteiro.starter.adapters.out.mail;

import com.brayanmonteiro.starter.domain.notification.MailMessage;
import com.brayanmonteiro.starter.domain.notification.MailPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class SmtpMailAdapter implements MailPort {
    private final JavaMailSender mailSender;
    private final String from;

    public SmtpMailAdapter(JavaMailSender mailSender, @Value("${app.mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void send(MailMessage message) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(from);
        mail.setTo(message.to());
        mail.setSubject(message.subject());
        mail.setText(message.body());
        mailSender.send(mail);
    }
}
