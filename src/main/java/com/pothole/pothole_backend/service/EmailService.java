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

    @Value("${app.backend.url:https://pothole-backend-gbud.onrender.com}")
    private String backendUrl;

    @Value("${app.demo.authority.email:nidhiqwerty2@gmail.com}")
    private String demoAuthorityEmail;

    @Async
    public void sendNewPotholeAlert(String toEmail, String zoneName,
                                    String severity, Double lat, Double lng,
                                    Integer potholeId, String reporterName) {

        // ALWAYS use demo email for now
        String actualEmail = demoAuthorityEmail;
        log.info("Preparing email: pothole={}, zone={}, to={}",
                potholeId, zoneName, actualEmail);

        String resolvedLink  = backendUrl + "/api/potholes/"
                + potholeId + "/quick-status?status=completed";
        String progressLink  = backendUrl + "/api/potholes/"
                + potholeId + "/quick-status?status=in_progress";
        String mapsLink      = "https://maps.google.com/?q=" + lat + "," + lng;

        String severityColor = switch (severity.toLowerCase()) {
            case "high"   -> "#dc3545";
            case "medium" -> "#fd7e14";
            default       -> "#28a745";
        };

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message, true, "UTF-8");

            helper.setTo(actualEmail);
            helper.setFrom("nidhiqwerty2@gmail.com");
            helper.setSubject("[POTHOLE ALERT] " + severity.toUpperCase()
                    + " severity — " + zoneName + " | #" + potholeId);

            String html = """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body style="margin:0;padding:0;background:#f0f2f5;
                      font-family:Arial,sans-serif;">
                <div style="max-width:600px;margin:30px auto;">

                  <!-- HEADER -->
                  <div style="background:#1a237e;padding:24px;
                       border-radius:8px 8px 0 0;text-align:center;">
                    <h1 style="color:white;margin:0;font-size:22px;">
                      🛣️ Smart Pothole Detection System
                    </h1>
                    <p style="color:#90caf9;margin:6px 0 0;font-size:13px;">
                      SATI Vidisha — Madhya Pradesh Road Safety
                    </p>
                  </div>

                  <!-- ALERT BANNER -->
                  <div style="background:%s;padding:16px;text-align:center;">
                    <span style="color:white;font-size:18px;font-weight:bold;">
                      ⚠️ %s SEVERITY POTHOLE REPORTED
                    </span>
                  </div>

                  <!-- BODY -->
                  <div style="background:white;padding:28px;">
                    <p style="color:#333;margin-top:0;">
                      Dear Zone Officer (<strong>%s</strong>),
                    </p>
                    <p style="color:#555;">
                      A new pothole has been reported in your zone.
                      Please take action at the earliest.
                    </p>

                    <!-- DETAILS TABLE -->
                    <table style="width:100%%;border-collapse:collapse;
                           margin:20px 0;">
                      <tr style="background:#e8eaf6;">
                        <td style="padding:10px 14px;border:1px solid #c5cae9;
                             font-weight:bold;color:#1a237e;width:40%%;">
                          Pothole ID
                        </td>
                        <td style="padding:10px 14px;border:1px solid #c5cae9;
                             color:#333;">
                          <strong>#%d</strong>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:10px 14px;border:1px solid #c5cae9;
                             font-weight:bold;color:#1a237e;">Zone</td>
                        <td style="padding:10px 14px;border:1px solid #c5cae9;
                             color:#333;">%s</td>
                      </tr>
                      <tr style="background:#e8eaf6;">
                        <td style="padding:10px 14px;border:1px solid #c5cae9;
                             font-weight:bold;color:#1a237e;">Severity</td>
                        <td style="padding:10px 14px;border:1px solid #c5cae9;">
                          <span style="background:%s;color:white;
                                padding:3px 12px;border-radius:12px;
                                font-weight:bold;">%s</span>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:10px 14px;border:1px solid #c5cae9;
                             font-weight:bold;color:#1a237e;">Reported By</td>
                        <td style="padding:10px 14px;border:1px solid #c5cae9;
                             color:#333;">%s</td>
                      </tr>
                      <tr style="background:#e8eaf6;">
                        <td style="padding:10px 14px;border:1px solid #c5cae9;
                             font-weight:bold;color:#1a237e;">GPS Location</td>
                        <td style="padding:10px 14px;border:1px solid #c5cae9;
                             color:#333;">%s, %s</td>
                      </tr>
                    </table>

                    <!-- ACTION BUTTONS -->
                    <div style="text-align:center;margin:28px 0 20px;">
                      <p style="color:#333;font-weight:bold;margin-bottom:16px;">
                        Click a button to update status:
                      </p>

                      <a href="%s"
                         style="display:inline-block;background:#28a745;
                                color:white;padding:14px 28px;border-radius:8px;
                                text-decoration:none;font-weight:bold;
                                font-size:15px;margin:6px;">
                        ✅ Mark as Completed
                      </a>

                      <a href="%s"
                         style="display:inline-block;background:#1976d2;
                                color:white;padding:14px 28px;border-radius:8px;
                                text-decoration:none;font-weight:bold;
                                font-size:15px;margin:6px;">
                        🔧 Mark In Progress
                      </a>

                      <a href="%s"
                         style="display:inline-block;background:#546e7a;
                                color:white;padding:14px 28px;border-radius:8px;
                                text-decoration:none;font-weight:bold;
                                font-size:15px;margin:6px;">
                        📍 View on Google Maps
                      </a>
                    </div>

                    <p style="color:#888;font-size:12px;
                         border-top:1px solid #eee;padding-top:16px;
                         text-align:center;margin-bottom:0;">
                      This is an automated alert from Smart Pothole Detection
                      System, SATI Vidisha. Do not reply to this email.
                    </p>
                  </div>

                </div>
                </body>
                </html>
                """.formatted(
                    severityColor,           // alert banner color
                    severity.toUpperCase(),  // alert banner text
                    zoneName,                // Dear Zone Officer
                    potholeId,               // Pothole ID
                    zoneName,                // Zone row
                    severityColor,           // severity badge color
                    severity.toUpperCase(),  // severity badge text
                    reporterName,            // Reported By
                    lat, lng,                // GPS
                    resolvedLink,            // Complete button
                    progressLink,            // In Progress button
                    mapsLink                 // Maps button
            );

            helper.setText(html, true);
            mailSender.send(message);
            log.info("✅ Email sent successfully to: {}", actualEmail);

        } catch (Exception e) {
            log.error("❌ Email FAILED to {}: {}", actualEmail, e.getMessage());
            log.error("Email error details: ", e);
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
            helper.setFrom("nidhiqwerty2@gmail.com");
            helper.setSubject("Your Pothole #" + id
                    + " is now " + status.toUpperCase());

            String color = switch (status.toLowerCase()) {
                case "completed"   -> "#28a745";
                case "in_progress" -> "#1976d2";
                case "rejected"    -> "#dc3545";
                default            -> "#ffc107";
            };

            String html = """
                <!DOCTYPE html>
                <html>
                <body style="font-family:Arial,sans-serif;background:#f0f2f5;
                      padding:30px;text-align:center;">
                <div style="max-width:500px;margin:0 auto;background:white;
                     border-radius:10px;overflow:hidden;
                     box-shadow:0 4px 20px rgba(0,0,0,0.1);">
                  <div style="background:#1a237e;padding:20px;">
                    <h2 style="color:white;margin:0;">🛣️ RoadWatch MP</h2>
                  </div>
                  <div style="padding:32px;">
                    <p style="color:#333;font-size:16px;">
                      Dear <strong>%s</strong>,
                    </p>
                    <p style="color:#555;">
                      Your pothole report has been updated!
                    </p>
                    <div style="background:%s;color:white;padding:16px 32px;
                         border-radius:8px;font-size:20px;font-weight:bold;
                         display:inline-block;margin:16px 0;">
                      %s
                    </div>
                    <p style="color:#555;">
                      Report ID: <strong>#%d</strong>
                    </p>
                    <p style="color:#888;font-size:12px;
                         margin-top:24px;padding-top:16px;
                         border-top:1px solid #eee;">
                      Thank you for helping make MP roads safer!<br>
                      Smart Pothole Detection System — SATI Vidisha
                    </p>
                  </div>
                </div>
                </body>
                </html>
                """.formatted(name, color, status.toUpperCase(), id);

            helper.setText(html, true);
            mailSender.send(message);
            log.info("Status email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("Status email failed: {}", e.getMessage());
        }
    }
}