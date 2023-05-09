package com.nix.managecafe.service;

import com.nix.managecafe.exception.BadRequestException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Component
public class EmailSender {
    private final JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String fromAddress;

    public EmailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendTo(String sendTo, String sender, String subject, String content) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        try {
            helper.setFrom(fromAddress, sender);
            helper.setTo(sendTo);
            helper.setSubject(subject);
            helper.setText(content, true);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new BadRequestException(e.getMessage());
        }
        mailSender.send(message);
    }
}
