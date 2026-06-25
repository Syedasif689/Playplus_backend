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

    @Value("${spring.mail.username}")       // Your actual email (e.g., asifsayed635@gmail.com)
    private String mailUsername;

    @Value("${spring.mail.properties.mail.from}")  // "Play+ <asifsayed635@gmail.com>"
    private String fromAddress;
    
    public void sendVerificationCode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailUsername);  // ✅ Shows "Play+" as sender name
        message.setTo(to);
        message.setSubject("Play+ Password Reset Verification Code");
        message.setText(
            "Hello,\n\n" +
            "You have requested to reset your password for Play+.\n\n" +
            "Your verification code is: " + code + "\n\n" +
            "This code will expire in 10 minutes.\n\n" +
            "If you did not request this, please ignore this email.\n\n" +
            "Regards,\n" +
            "Play+ Team"
        );
        System.out.println("MAIL USER = " + mailUsername);
        System.out.println("Sending mail to = " + to);
        mailSender.send(message);
    }
    
    public void sendPasswordResetConfirmation(String to) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailUsername);  // ✅ Same here
        message.setTo(to);
        message.setSubject("Play+ Password Reset Confirmation");
        message.setText(
            "Hello,\n\n" +
            "Your password has been successfully reset for Play+.\n\n" +
            "If you did not perform this action, please contact support immediately.\n\n" +
            "Regards,\n" +
            "Play+ Team"
        );
        mailSender.send(message);
    }
}