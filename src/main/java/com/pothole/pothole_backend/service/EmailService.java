package com.pothole.pothole_backend.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    // Backend URL for status update links
    @Value("${app.backend.url:https://pothole-backend-gbud.onrender.com}")
    private String backendUrl;

    // For demo: authority email override
    @Value("${app.demo.authority.email:pragyasirse@gmail.com}")
    private String demoAuthorityEmail;

    @Async
    public void sendNewPotholeAlert(String toEmail, String zoneName,
                                    String severity, Double lat, Double lng,
                                    Integer potholeId, String reporterName) {
        // For demo — always send to Pragya's email
        String actualEmail = demoAuthorityEmail;
        log.info("Sending alert to demo email: {} (original: {})",
                actualEmail, toEmail);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message, true, "UTF-8");

            helper.setTo(actualEmail);
            helper.setSubject("[ACTION REQUIRED] New " + severity.toUpperCase()
                    + " Pothole — " + zoneName);

            String resolvedLink = backendUrl
                    + "/api/potholes/" + potholeId + "/quick-status?status=completed";
            String progressLink = backendUrl
                    + "/api/potholes/" + potholeId + "/quick-status?status=in_progress";
            String mapsLink = "https://maps.google.com/?q=" + lat + "," + lng;

            String html = """
                <div style="font-family:Arial,sans-serif;max-width:600px;
                     margin:0 auto;background:#f5f5f5;padding:20px;">
                  <div style="background:#1F3864;padding:20px;
                       border-radius:8px 8px 0 0;text-align:center;">
                    <h2 style="color:white;margin:0;">
                      🚨 Smart Pothole Detection System
                    </h2>
                    <p style="color:#ccc;margin:8px 0 0;">
                      Madhya Pradesh Road Safety
                    </p>
                  </div>

                  <div style="background:white;padding:24px;
                       border-radius:0 0 8px 8px;">
                    <div style="background:%s;padding:12px 16px;
                         border-radius:6px;margin-bottom:20px;">
                      <strong style="color:white;font-size:16px;">
                        %s SEVERITY POTHOLE REPORTED
                      </strong>
                    </div>

                    <table style="width:100%%;border-collapse:collapse;
                           margin-bottom:20px;">
                      <tr style="background:#f8f9fa;">
                        <td style="padding:10px;border:1px solid #dee2e6;
                             font-weight:bold;width:35%%;">Pothole ID</td>
                        <td style="padding:10px;border:1px solid #dee2e6;">
                          #%d
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:10px;border:1px solid #dee2e6;
                             font-weight:bold;">Zone</td>
                        <td style="padding:10px;border:1px solid #dee2e6;">
                          %s
                        </td>
                      </tr>
                      <tr style="background:#f8f9fa;">
                        <td style="padding:10px;border:1px solid #dee2e6;
                             font-weight:bold;">Severity</td>
                        <td style="padding:10px;border:1px solid #dee2e6;">
                          <strong style="color:%s;">%s</strong>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:10px;border:1px solid #dee2e6;
                             font-weight:bold;">Reported By</td>
                        <td style="padding:10px;border:1px solid #dee2e6;">
                          %s
                        </td>
                      </tr>
                      <tr style="background:#f8f9fa;">
                        <td style="padding:10px;border:1px solid #dee2e6;
                             font-weight:bold;">Location</td>
                        <td style="padding:10px;border:1px solid #dee2e6;">
                          %s, %s
                        </td>
                      </tr>
                    </table>

                    <div style="text-align:center;margin:24px 0;">
                      <p style="margin-bottom:12px;font-weight:bold;
                           color:#333;">
                        Click to Update Status:
                      </p>
                      <a href="%s"
                         style="background:#28a745;color:white;padding:12px 24px;
                                border-radius:6px;text-decoration:none;
                                font-weight:bold;display:inline-block;margin:6px;">
                        ✅ Mark as Completed
                      </a>
                      <a href="%s"
                         style="background:#007bff;color:white;padding:12px 24px;
                                border-radius:6px;text-decoration:none;
                                font-weight:bold;display:inline-block;margin:6px;">
                        🔧 Mark In Progress
                      </a>
                      <a href="%s"
                         style="background:#6c757d;color:white;padding:12px 24px;
                                border-radius:6px;text-decoration:none;
                                font-weight:bold;display:inline-block;margin:6px;">
                        📍 View on Maps
                      </a>
                    </div>

                    <p style="color:#666;font-size:13px;
                         border-top:1px solid #eee;padding-top:16px;
                         text-align:center;">
                      Smart Pothole Detection System — SATI Vidisha, MP
                    </p>
                  </div>
                </div>
                """.formatted(
                    severity.equals("high") ? "#dc3545" :
                            severity.equals("medium") ? "#ffc107" : "#28a745",
                    severity.toUpperCase(),
                    potholeId,
                    zoneName,
                    severity.equals("high") ? "#dc3545" :
                            severity.equals("medium") ? "#856404" : "#155724",
                    severity.toUpperCase(),
                    reporterName,
                    lat, lng,
                    resolvedLink,
                    progressLink,
                    mapsLink
            );

            helper.setText(html, true);
            mailSender.send(message);
            log.info("Alert email sent successfully to: {}", actualEmail);

        } catch (Exception e) {
            log.error("Email send failed: {}", e.getMessage());
        }
    }

    @Async
    public void sendStatusUpdate(String toEmail, String name,
                                 Integer id, String status) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Pothole #" + id + " Status Updated — "
                    + status.toUpperCase());

            String statusColor = status.equals("completed") ? "#28a745" :
                    status.equals("in_progress") ? "#007bff" :
                            status.equals("rejected") ? "#dc3545" : "#ffc107";

            String html = """
                <div style="font-family:Arial,sans-serif;max-width:600px;
                     margin:0 auto;background:#f5f5f5;padding:20px;">
                  <div style="background:#1F3864;padding:20px;
                       border-radius:8px 8px 0 0;text-align:center;">
                    <h2 style="color:white;margin:0;">
                      🛣️ RoadWatch MP
                    </h2>
                  </div>
                  <div style="background:white;padding:24px;
                       border-radius:0 0 8px 8px;text-align:center;">
                    <h3 style="color:#333;">
                      Your Pothole Report #%d Has Been Updated
                    </h3>
                    <div style="background:%s;color:white;padding:16px 32px;
                         border-radius:8px;display:inline-block;
                         font-size:18px;font-weight:bold;margin:16px 0;">
                      %s
                    </div>
                    <p style="color:#555;">
                      Dear %s, thank you for helping make
                      Madhya Pradesh roads safer!
                    </p>
                    <p style="color:#888;font-size:13px;
                         border-top:1px solid #eee;padding-top:16px;">
                      Smart Pothole Detection System — SATI Vidisha
                    </p>
                  </div>
                </div>
                """.formatted(id, statusColor, status.toUpperCase(), name);

            helper.setText(html, true);
            mailSender.send(message);
            log.info("Status email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("Status email failed: {}", e.getMessage());
        }
    }
}