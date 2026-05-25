package com.civicsense.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // ✅ Use property instead of hardcoding
    @Value("${spring.mail.username}")
    private String fromEmail;

    // =========================
    // SEND PASSWORD RESET EMAIL
    // =========================
    public void sendResetEmail(String toEmail, String token) {
        try {
            String resetLink = "http://localhost:8080/reset-password?token=" + token;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("CivicSense | Password Reset Request");

            message.setText(
                    "Hello,\n\n" +
                    "We received a request to reset your CivicSense account password.\n\n" +
                    "Click the link below to reset your password:\n\n" +
                    resetLink + "\n\n" +
                    "This link will expire in 30 minutes.\n\n" +
                    "If you did not request a password reset, please ignore this email.\n\n" +
                    "Regards,\n" +
                    "CivicSense Support Team"
            );

            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ========================================
    // SEND COMPLAINT STATUS EMAIL
    // ========================================
    public void sendComplaintStatusEmail(String toEmail, String userName, Long complaintId, String status) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("CivicSense | Complaint Status Update");

            message.setText(
                    "Hello " + userName + ",\n\n" +
                    "Your complaint #" + complaintId + " status has been updated to: " + status + ".\n\n" +
                    "Thank you for using CivicSense.\n\n" +
                    "Regards,\n" +
                    "CivicSense Support Team"
            );

            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // NEW: GENERIC EMAIL METHOD
    // =========================
    public void sendEmail(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ✅ Optional: Future method for CC/BCC if needed
    public void sendEmailWithCcBcc(String toEmail, String ccEmail, String bccEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);

            if (ccEmail != null && !ccEmail.isEmpty()) message.setCc(ccEmail);
            if (bccEmail != null && !bccEmail.isEmpty()) message.setBcc(bccEmail);

            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}