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
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(toEmail);
            msg.setSubject("New " + severity.toUpperCase() +
                    " Pothole Reported - " + zoneName);
            msg.setText(
                    "Zone: " + zoneName + "\n" +
                            "Severity: " + severity.toUpperCase() + "\n" +
                            "Location: https://maps.google.com/?q=" + lat + "," + lng + "\n\n" +
                            "Please take immediate action.\n- Pothole Detection System MP"
            );
            mailSender.send(msg);
            log.info("Alert email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Email failed: {}", e.getMessage());
        }
    }

    @Async
    public void sendStatusUpdate(String toEmail, String name,
                                 Integer id, String status) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(toEmail);
            msg.setSubject("Pothole #" + id + " Status: " + status.toUpperCase());
            msg.setText(
                    "Dear " + name + ",\n\n" +
                            "Your pothole report #" + id +
                            " status has been updated to: " + status.toUpperCase() +
                            "\n\nThank you!\n- Pothole Detection System MP"
            );
            mailSender.send(msg);
            log.info("Status email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Status email failed: {}", e.getMessage());
        }

    }
}
