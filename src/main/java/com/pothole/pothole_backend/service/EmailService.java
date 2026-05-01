package com.pothole.pothole_backend.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import sibApi.TransactionalEmailsApi;
import sibModel.*;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.sender.email:pragyapp12@gmail.com}")
    private String senderEmail;

    @Value("${brevo.sender.name:RoadWatch MP}")
    private String senderName;

    @Value("${app.backend.url:https://pothole-backend-gbud.onrender.com}")
    private String backendUrl;

    @Value("${app.demo.authority.email:pragyapp12@gmail.com}")
    private String demoAuthorityEmail;

    @Async
    public void sendNewPotholeAlert(String toEmail, String zoneName,
                                    String severity, Double lat, Double lng,
                                    Integer potholeId, String reporterName) {

        String actualEmail = demoAuthorityEmail;
        log.info("Sending alert email to: {}", actualEmail);

        try {
            TransactionalEmailsApi apiInstance = new TransactionalEmailsApi();
            apiInstance.getApiClient().setApiKey(apiKey);

            String completedLink  = backendUrl + "/api/potholes/"
                    + potholeId + "/quick-status?status=completed";
            String progressLink   = backendUrl + "/api/potholes/"
                    + potholeId + "/quick-status?status=in_progress";
            String mapsLink       = "https://maps.google.com/?q=" + lat + "," + lng;

            String severityColor  = severity.equalsIgnoreCase("high")   ? "#dc3545" :
                    severity.equalsIgnoreCase("medium")  ? "#fd7e14" :
                            "#28a745";

            String html = """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body style="margin:0;padding:0;background:#f0f2f5;
                      font-family:Arial,sans-serif;">
                <div style="max-width:600px;margin:30px auto;">

                  <div style="background:#1a237e;padding:24px;
                       border-radius:8px 8px 0 0;text-align:center;">
                    <h1 style="color:white;margin:0;font-size:22px;">
                      🛣️ Smart Pothole Detection System
                    </h1>
                    <p style="color:#90caf9;margin:6px 0 0;font-size:13px;">
                      SATI Vidisha — Madhya Pradesh
                    </p>
                  </div>

                  <div style="background:%s;padding:14px;text-align:center;">
                    <span style="color:white;font-size:17px;font-weight:bold;">
                      ⚠️ %s SEVERITY POTHOLE REPORTED
                    </span>
                  </div>

                  <div style="background:white;padding:28px;">
                    <p style="color:#333;margin-top:0;">
                      Dear Zone Officer (<strong>%s</strong>),
                    </p>
                    <p style="color:#555;margin-bottom:20px;">
                      A new pothole has been reported. Please take action.
                    </p>

                    <table style="width:100%%;border-collapse:collapse;
                           margin-bottom:24px;">
                      <tr style="background:#e8eaf6;">
                        <td style="padding:10px 14px;border:1px solid #c5cae9;
                             font-weight:bold;color:#1a237e;width:38%%;">
                             Pothole ID</td>
                        <td style="padding:10px 14px;border:1px solid #c5cae9;">
                             <strong>#%d</strong></td>
                      </tr>
                      <tr>
                        <td style="padding:10px 14px;border:1px solid #c5cae9;
                             font-weight:bold;color:#1a237e;">Zone</td>
                        <td style="padding:10px 14px;border:1px solid #c5cae9;">
                             %s</td>
                      </tr>
                      <tr style="background:#e8eaf6;">
                        <td style="padding:10px 14px;border:1px solid #c5cae9;
                             font-weight:bold;color:#1a237e;">Severity</td>
                        <td style="padding:10px 14px;border:1px solid #c5cae9;">
                          <span style="background:%s;color:white;padding:3px 12px;
                                border-radius:12px;font-weight:bold;">
                                %s</span></td>
                      </tr>
                      <tr>
                        <td style="padding:10px 14px;border:1px solid #c5cae9;
                             font-weight:bold;color:#1a237e;">Reported By</td>
                        <td style="padding:10px 14px;border:1px solid #c5cae9;">
                             %s</td>
                      </tr>
                      <tr style="background:#e8eaf6;">
                        <td style="padding:10px 14px;border:1px solid #c5cae9;
                             font-weight:bold;color:#1a237e;">Location</td>
                        <td style="padding:10px 14px;border:1px solid #c5cae9;">
                             %s, %s</td>
                      </tr>
                    </table>

                    <div style="text-align:center;margin:24px 0;">
                      <p style="color:#333;font-weight:bold;margin-bottom:16px;">
                        Update Status by clicking:
                      </p>
                      <a href="%s"
                         style="display:inline-block;background:#28a745;
                                color:white;padding:13px 26px;border-radius:7px;
                                text-decoration:none;font-weight:bold;
                                font-size:15px;margin:5px;">
                        ✅ Mark Completed
                      </a>
                      <a href="%s"
                         style="display:inline-block;background:#1976d2;
                                color:white;padding:13px 26px;border-radius:7px;
                                text-decoration:none;font-weight:bold;
                                font-size:15px;margin:5px;">
                        🔧 Mark In Progress
                      </a>
                      <a href="%s"
                         style="display:inline-block;background:#546e7a;
                                color:white;padding:13px 26px;border-radius:7px;
                                text-decoration:none;font-weight:bold;
                                font-size:15px;margin:5px;">
                        📍 Google Maps
                      </a>
                    </div>

                    <p style="color:#999;font-size:11px;text-align:center;
                         border-top:1px solid #eee;padding-top:14px;margin:0;">
                      Automated alert — Smart Pothole Detection System,
                      SATI Vidisha
                    </p>
                  </div>
                </div>
                </body>
                </html>
                """.formatted(
                    severityColor, severity.toUpperCase(),
                    zoneName,
                    potholeId,
                    zoneName,
                    severityColor, severity.toUpperCase(),
                    reporterName,
                    lat, lng,
                    completedLink, progressLink, mapsLink
            );

            SendSmtpEmail email = new SendSmtpEmail();
            email.setSender(new SendSmtpEmailSender()
                    .email(senderEmail).name(senderName));
            email.setTo(Collections.singletonList(
                    new SendSmtpEmailTo().email(actualEmail)));
            email.setSubject("[POTHOLE ALERT] #" + potholeId
                    + " — " + severity.toUpperCase() + " — " + zoneName);
            email.setHtmlContent(html);

            apiInstance.sendTransacEmail(email);
            log.info("✅ Email sent via Brevo to {}", actualEmail);

        } catch (Exception e) {
            log.error("❌ Brevo email failed: {}", e.getMessage(), e);
        }
    }

    @Async
    public void sendStatusUpdate(String toEmail, String name,
                                 Integer id, String status) {
        try {
            TransactionalEmailsApi apiInstance = new TransactionalEmailsApi();
            apiInstance.getApiClient().setApiKey(apiKey);

            String color = status.equalsIgnoreCase("completed")  ? "#28a745" :
                    status.equalsIgnoreCase("in_progress") ? "#1976d2" :
                            status.equalsIgnoreCase("rejected")    ? "#dc3545" :
                                    "#ffc107";

            String html = """
                <!DOCTYPE html>
                <html>
                <body style="font-family:Arial,sans-serif;background:#f0f2f5;
                      padding:30px;text-align:center;margin:0;">
                <div style="max-width:500px;margin:0 auto;background:white;
                     border-radius:10px;overflow:hidden;
                     box-shadow:0 4px 20px rgba(0,0,0,0.1);">
                  <div style="background:#1a237e;padding:20px;">
                    <h2 style="color:white;margin:0;font-size:20px;">
                      🛣️ RoadWatch MP
                    </h2>
                  </div>
                  <div style="padding:32px;">
                    <p style="color:#333;font-size:16px;">
                      Dear <strong>%s</strong>,
                    </p>
                    <p style="color:#555;">
                      Your pothole report has been updated!
                    </p>
                    <div style="background:%s;color:white;padding:14px 28px;
                         border-radius:8px;font-size:18px;font-weight:bold;
                         display:inline-block;margin:16px 0;">
                      %s
                    </div>
                    <p style="color:#666;margin-top:12px;">
                      Report ID: <strong>#%d</strong>
                    </p>
                    <p style="color:#999;font-size:12px;margin-top:24px;
                         padding-top:14px;border-top:1px solid #eee;">
                      Thank you for helping make MP roads safer!<br>
                      Smart Pothole Detection System — SATI Vidisha
                    </p>
                  </div>
                </div>
                </body>
                </html>
                """.formatted(name, color, status.toUpperCase(), id);

            SendSmtpEmail email = new SendSmtpEmail();
            email.setSender(new SendSmtpEmailSender()
                    .email(senderEmail).name(senderName));
            email.setTo(Collections.singletonList(
                    new SendSmtpEmailTo().email(toEmail)));
            email.setSubject("Pothole #" + id
                    + " Status: " + status.toUpperCase());
            email.setHtmlContent(html);

            apiInstance.sendTransacEmail(email);
            log.info("✅ Status email sent via Brevo to {}", toEmail);

        } catch (Exception e) {
            log.error("❌ Status email failed: {}", e.getMessage(), e);
        }
    }
}
