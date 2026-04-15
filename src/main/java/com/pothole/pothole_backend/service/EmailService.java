package com.pothole.pothole_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendNewPotholeAlert(String toEmail, String zoneName,
                                    String severity, Double lat, Double lng) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("🚨 New " + severity.toUpperCase() +
                    " Severity Pothole Reported - " + zoneName);
            message.setText(
                    "A new pothole has been reported in your zone.\n\n" +
                            "Zone: " + zoneName + "\n" +
                            "Severity: " + severity.toUpperCase() + "\n" +
                            "Location: " + lat + ", " + lng + "\n" +
                            "Google Maps: https://maps.google.com/?q=" + lat + "," + lng + "\n\n" +
                            "Please take action at the earliest.\n\n" +
                            "- Smart Pothole Detection System, MP"
            );
            mailSender.send(message);
            log.info("Email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendStatusUpdateToUser(String toEmail, String userName,
                                       Integer potholeId, String newStatus) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("✅ Pothole Report #" + potholeId + " Status Updated");
            message.setText(
                    "Dear " + userName + ",\n\n" +
                            "Your reported pothole (ID: #" + potholeId + ") " +
                            "status has been updated to: " + newStatus.toUpperCase() + "\n\n" +
                            "Thank you for contributing to better roads in Madhya Pradesh!\n\n" +
                            "- Smart Pothole Detection System, MP"
            );
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send status update email: {}", e.getMessage());
        }
    }
}
