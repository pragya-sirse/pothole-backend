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

    // ── Authority ko pothole alert bhejo ──────────────────
    @Async
    public void sendNewPotholeAlert(String toEmail, String zoneName,
                                    String severity, Double lat, Double lng,
                                    Integer potholeId, String reporterName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("[ACTION REQUIRED] " + severity.toUpperCase()
                    + " Severity Pothole — " + zoneName + " | ID #" + potholeId);

            String mapsUrl = "https://maps.google.com/?q=" + lat + "," + lng;
            String resolveUrl = backendUrl + "/api/potholes/" + potholeId
                    + "/quick-update?status=completed&token=auth" + potholeId;
            String progressUrl = backendUrl + "/api/potholes/" + potholeId
                    + "/quick-update?status=in_progress&token=auth" + potholeId;

            String html = "<!DOCTYPE html><html><body style='font-family:Arial,sans-serif;"
                    + "background:#f5f5f5;margin:0;padding:20px'>"
                    + "<div style='max-width:600px;margin:0 auto;background:white;"
                    + "border-radius:10px;overflow:hidden;box-shadow:0 2px 10px rgba(0,0,0,0.1)'>"

                    // Header
                    + "<div style='background:" + severityColor(severity) + ";padding:24px;text-align:center'>"
                    + "<h2 style='color:white;margin:0;font-size:20px'>"
                    + severity.toUpperCase() + " SEVERITY POTHOLE REPORTED</h2>"
                    + "<p style='color:rgba(255,255,255,0.9);margin:8px 0 0'>Pothole ID: #" + potholeId + "</p>"
                    + "</div>"

                    // Details
                    + "<div style='padding:24px'>"
                    + "<table style='width:100%;border-collapse:collapse'>"
                    + detailRow("Zone", zoneName)
                    + detailRow("Severity", severity.toUpperCase())
                    + detailRow("Reported By", reporterName)
                    + detailRow("Coordinates", lat + ", " + lng)
                    + "</table>"

                    // Map Link
                    + "<div style='margin:20px 0;text-align:center'>"
                    + "<a href='" + mapsUrl + "' style='background:#4285f4;color:white;"
                    + "padding:12px 24px;border-radius:6px;text-decoration:none;"
                    + "display:inline-block;font-weight:bold'>View on Google Maps</a>"
                    + "</div>"

                    // Action Buttons
                    + "<h3 style='color:#333;margin:24px 0 12px'>Take Action:</h3>"
                    + "<div style='display:flex;gap:12px;flex-wrap:wrap'>"

                    + "<a href='" + progressUrl + "' style='background:#f59e0b;color:white;"
                    + "padding:14px 24px;border-radius:6px;text-decoration:none;"
                    + "font-weight:bold;display:inline-block;margin:4px'>"
                    + "Mark as In Progress</a>"

                    + "<a href='" + resolveUrl + "' style='background:#10b981;color:white;"
                    + "padding:14px 24px;border-radius:6px;text-decoration:none;"
                    + "font-weight:bold;display:inline-block;margin:4px'>"
                    + "Mark as Resolved</a>"

                    + "</div>"

                    + "<p style='color:#888;font-size:12px;margin-top:24px'>"
                    + "This is an automated alert from Smart Pothole Detection System, "
                    + "Madhya Pradesh. Clicking the buttons above will instantly update "
                    + "the repair status and notify the citizen.</p>"
                    + "</div>"

                    // Footer
                    + "<div style='background:#f8f8f8;padding:16px;text-align:center;"
                    + "border-top:1px solid #eee'>"
                    + "<p style='color:#888;font-size:12px;margin:0'>"
                    + "RoadWatch MP — Smart Pothole Detection System</p>"
                    + "</div>"
                    + "</div></body></html>";

            helper.setText(html, true);
            mailSender.send(message);
            log.info("Alert email sent to authority: {} for pothole #{}", toEmail, potholeId);

        } catch (Exception e) {
            log.error("Failed to send alert email to {}: {}", toEmail, e.getMessage());
        }
    }

    // ── Citizen ko status update bhejo ────────────────────
    @Async
    public void sendStatusUpdate(String toEmail, String name,
                                 Integer id, String status) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Pothole Report #" + id + " — Status: "
                    + status.replace("_", " ").toUpperCase());

            String statusColor = statusColor(status);
            String statusIcon  = statusIcon(status);

            String html = "<!DOCTYPE html><html><body style='font-family:Arial,sans-serif;"
                    + "background:#f5f5f5;margin:0;padding:20px'>"
                    + "<div style='max-width:500px;margin:0 auto;background:white;"
                    + "border-radius:10px;overflow:hidden;box-shadow:0 2px 10px rgba(0,0,0,0.1)'>"

                    + "<div style='background:#1a2235;padding:20px;text-align:center'>"
                    + "<h2 style='color:white;margin:0;font-size:18px'>RoadWatch MP</h2>"
                    + "</div>"

                    + "<div style='padding:32px;text-align:center'>"
                    + "<div style='font-size:48px'>" + statusIcon + "</div>"
                    + "<h2 style='color:#333;margin:16px 0 8px'>Report #" + id + " Updated</h2>"
                    + "<p style='color:#666;margin-bottom:24px'>Dear " + name + ",</p>"

                    + "<div style='background:" + statusColor + "22;border:2px solid "
                    + statusColor + ";border-radius:8px;padding:16px;margin:16px 0'>"
                    + "<p style='color:" + statusColor + ";font-weight:bold;font-size:18px;margin:0'>"
                    + status.replace("_", " ").toUpperCase() + "</p>"
                    + "</div>"

                    + "<p style='color:#666;line-height:1.6'>"
                    + statusMessage(status) + "</p>"

                    + "<p style='color:#888;font-size:12px;margin-top:32px'>"
                    + "Thank you for helping make Madhya Pradesh roads safer!</p>"
                    + "</div>"

                    + "<div style='background:#f8f8f8;padding:14px;text-align:center;"
                    + "border-top:1px solid #eee'>"
                    + "<p style='color:#aaa;font-size:11px;margin:0'>"
                    + "Smart Pothole Detection System, Madhya Pradesh</p>"
                    + "</div>"
                    + "</div></body></html>";

            helper.setText(html, true);
            mailSender.send(message);
            log.info("Status update email sent to citizen: {} for pothole #{}", toEmail, id);

        } catch (Exception e) {
            log.error("Failed to send status email: {}", e.getMessage());
        }
    }

    // ── Helper methods ─────────────────────────────────────
    private String detailRow(String label, String value) {
        return "<tr><td style='padding:8px;color:#888;font-size:13px;width:120px'>"
                + label + ":</td>"
                + "<td style='padding:8px;color:#333;font-weight:bold;font-size:13px'>"
                + value + "</td></tr>";
    }

    private String severityColor(String severity) {
        return switch (severity.toLowerCase()) {
            case "high"   -> "#ef4444";
            case "medium" -> "#f59e0b";
            default       -> "#10b981";
        };
    }

    private String statusColor(String status) {
        return switch (status.toLowerCase()) {
            case "completed"   -> "#10b981";
            case "in_progress" -> "#3b82f6";
            case "rejected"    -> "#ef4444";
            default            -> "#f59e0b";
        };
    }

    private String statusIcon(String status) {
        return switch (status.toLowerCase()) {
            case "completed"   -> "✅";
            case "in_progress" -> "🔧";
            case "rejected"    -> "❌";
            default            -> "⏳";
        };
    }

    private String statusMessage(String status) {
        return switch (status.toLowerCase()) {
            case "completed"   ->
                    "Great news! Your reported pothole has been repaired. "
                            + "Thank you for reporting it and helping improve our roads.";
            case "in_progress" ->
                    "Your reported pothole is currently being repaired. "
                            + "Our team is working on it. We will notify you once completed.";
            case "rejected"    ->
                    "Your report was reviewed but could not be processed at this time. "
                            + "This may be due to the location being outside our service area "
                            + "or the issue being already addressed.";
            default ->
                    "Your pothole report has been received and is under review. "
                            + "We will update you as soon as action is taken.";
        };
    }
}