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
                                    String severity, Double lat, Double lng,
                                    Integer potholeId, String reporterName) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(toEmail);
            msg.setSubject("[URGENT] New " + severity.toUpperCase()
                    + " Pothole Reported — " + zoneName);
            msg.setText(
                    "Dear Zonal Officer,\n\n"
                            + "A new pothole has been reported in your zone. Details:\n\n"
                            + "  Pothole ID   : #" + potholeId + "\n"
                            + "  Zone         : " + zoneName + "\n"
                            + "  Severity     : " + severity.toUpperCase() + "\n"
                            + "  Reported By  : " + reporterName + "\n"
                            + "  Location     : " + lat + ", " + lng + "\n"
                            + "  Google Maps  : https://maps.google.com/?q=" + lat + "," + lng + "\n\n"
                            + "Please take necessary action at the earliest.\n\n"
                            + "— Smart Pothole Detection System, Madhya Pradesh"
            );
            mailSender.send(msg);
            log.info("Alert email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Email send failed to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendStatusUpdate(String toEmail, String name,
                                 Integer id, String status) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(toEmail);
            msg.setSubject("Pothole Report #" + id + " — Status Updated to "
                    + status.toUpperCase());
            msg.setText(
                    "Dear " + name + ",\n\n"
                            + "Your pothole report #" + id + " has been updated.\n\n"
                            + "  New Status: " + status.toUpperCase() + "\n\n"
                            + "Thank you for helping make Madhya Pradesh roads safer!\n\n"
                            + "— Smart Pothole Detection System, Madhya Pradesh"
            );
            mailSender.send(msg);
            log.info("Status email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Status email failed: {}", e.getMessage());
        }
    }
}
