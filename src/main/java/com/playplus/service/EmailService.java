package com.playplus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailUsername;

    @Value("${spring.mail.properties.mail.from}")
    private String fromAddress;

    public void sendVerificationCode(String to, String code) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject("Play+ Password Reset Verification Code");
        message.setText(
            "Welcome to Play+\n\n" +
            "Your verification code is: " + code + "\n\n" +
            "This code will expire in 10 minutes.\n\n" +
            "If you did not request this code, please ignore this email.\n\n" +
            "Thanks,\n" +
            "Play+ Team"
          );

        try {

            System.out.println("MAIL USER = " + mailUsername);
            System.out.println("Sending mail to = " + to);

            mailSender.send(message);

            System.out.println("✅ EMAIL SENT SUCCESSFULLY");

        } catch (Exception e) {

            System.out.println("❌ EMAIL ERROR:");
            e.printStackTrace();

            throw new RuntimeException("Email sending failed", e);
        }
    }

    /**public void sendPasswordResetConfirmation(String to) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject("Play+ Password Reset Confirmation");
        message.setText(
            "Hello,\n\n" +
            "Your password has been successfully reset for Play+."
        );

        mailSender.send(message);
    }/** */
}