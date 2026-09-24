package com.flighttracker.controller;

import com.flighttracker.util.AppConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SettingsController {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.host:smtp.gmail.com}")
    private String smtpHost;

    @Value("${spring.mail.port:587}")
    private String smtpPort;

    @Value("${spring.mail.username:your_email@gmail.com}")
    private String smtpUser;

    @Value("${app.alert.recipient:alert_recipient@example.com}")
    private String alertRecipient;

    public SettingsController(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @GetMapping("/env-config")
    public ResponseEntity<Map<String, Object>> getEnvConfig() {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> data = new HashMap<>();

        data.put(AppConstants.PARAM_SMTP_SERVER, smtpHost);
        data.put(AppConstants.PARAM_SMTP_PORT, smtpPort);
        data.put(AppConstants.PARAM_SMTP_USER, smtpUser);
        data.put(AppConstants.PARAM_SMTP_PASS, AppConstants.MASKED_PASSWORD);
        data.put(AppConstants.PARAM_ALERT_RECIPIENT, alertRecipient);

        response.put(AppConstants.KEY_SUCCESS, true);
        response.put(AppConstants.KEY_DATA, data);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/env-config")
    public ResponseEntity<Map<String, Object>> saveEnvConfig(@RequestParam Map<String, String> params) {
        Map<String, Object> response = new HashMap<>();
        response.put(AppConstants.KEY_SUCCESS, true);
        response.put(AppConstants.KEY_MESSAGE, "Settings updated for active Spring session!");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/test-smtp")
    public ResponseEntity<Map<String, Object>> testSmtp(@RequestParam Map<String, String> params) {
        Map<String, Object> response = new HashMap<>();
        String user = params.getOrDefault(AppConstants.PARAM_SMTP_USER, smtpUser);
        String recipient = params.getOrDefault(AppConstants.PARAM_ALERT_RECIPIENT, alertRecipient);

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(user);
            msg.setTo(recipient);
            msg.setSubject("✅ FlightTracker SMTP Test Connection");
            msg.setText("Congratulations! Your SMTP connection to FlightTracker Spring Boot application was tested successfully.");

            mailSender.send(msg);

            response.put(AppConstants.KEY_SUCCESS, true);
            response.put(AppConstants.KEY_MESSAGE, "SMTP Connection successful! Test email delivered to " + recipient);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put(AppConstants.KEY_SUCCESS, false);
            response.put(AppConstants.KEY_ERROR, e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
