package com.pothole.pothole_backend.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    @Value("${app.backend.url:https://pothole-backend-gbud.onrender.com}")
    private String backendUrl;

    @Value("${app.demo.authority.email:nidhiqwerty2@gmail.com}")
    private String demoAuthorityEmail;

    @Async
    public void sendNewPotholeAlert(String toEmail, String zoneName,
                                    String severity, Double lat, Double lng,
                                    Integer potholeId, String reporterName) {

        String actualEmail = demoAuthorityEmail;

        try {
            sibApi.TransactionalEmailsApi apiInstance = new sibApi.TransactionalEmailsApi();
            apiInstance.getApiClient().setApiKey(apiKey);

            String mapsLink = "https://maps.google.com/?q=" + lat + "," + lng;

            String html = "<h3>🚧 New Pothole Alert</h3>" +
                    "<p><b>Zone:</b> " + zoneName + "</p>" +
                    "<p><b>Severity:</b> " + severity + "</p>" +
                    "<p><b>Reported By:</b> " + reporterName + "</p>" +
                    "<p><a href='" + mapsLink + "'>View Location</a></p>";

            sibModel.SendSmtpEmail email = new sibModel.SendSmtpEmail();

            email.setSender(new sibModel.SendSmtpEmailSender()
                    .email(senderEmail)
                    .name(senderName));

            email.setTo(java.util.Collections.singletonList(
                    new sibModel.SendSmtpEmailTo().email(actualEmail)
            ));

            email.setSubject("[POTHOLE ALERT] " + severity + " - " + zoneName);
            email.setHtmlContent(html);

            apiInstance.sendTransacEmail(email);

            log.info("✅ Email sent via Brevo to {}", actualEmail);

        } catch (Exception e) {
            log.error("❌ Brevo email failed: {}", e.getMessage(), e);
        }
    }
}
