package com.flighttracker.service;

import com.flighttracker.entity.AlertLog;
import com.flighttracker.entity.PriceHistory;
import com.flighttracker.entity.SearchConfig;
import com.flighttracker.repository.AlertLogRepository;
import com.flighttracker.util.EncryptionUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Service
public class AlertService {

    private final AlertLogRepository alertLogRepository;
    private final HttpClient httpClient;

    @Value("${app.security.secret-key:FlightTrackerSecretKey2026#SecureAES}")
    private String secretKey;

    @Value("${spring.mail.host:smtp.gmail.com}")
    private String defaultSmtpHost;

    @Value("${spring.mail.port:587}")
    private int defaultSmtpPort;

    @Value("${spring.mail.username:your_email@gmail.com}")
    private String defaultSmtpUser;

    @Value("${spring.mail.password:your_app_password}")
    private String defaultSmtpPass;

    @Value("${app.alert.recipient:alert_recipient@example.com}")
    private String defaultRecipient;

    public AlertService(AlertLogRepository alertLogRepository) {
        this.alertLogRepository = alertLogRepository;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public void processPriceAlert(SearchConfig config, PriceHistory flight) {
        String method = config.getAlertMethod();
        if ("NONE".equalsIgnoreCase(method)) {
            // User requested dashboard-only tracking without alerts
            return;
        }

        BigDecimal budget = BigDecimal.valueOf(config.getBudgetThreshold());
        if (flight.getPriceInr().compareTo(budget) <= 0) {
            LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
            List<AlertLog> recentAlerts = alertLogRepository.findRecentAlertsForDate(
                    config.getId(), cutoff, flight.getFlightDate().toString()
            );

            if (recentAlerts.isEmpty()) {
                String alertMsg = String.format("Price drop alert! %s %s on %s is now ₹%s",
                        flight.getAirline(), flight.getFlightNumber(), flight.getFlightDate(), flight.getPriceInr());

                AlertLog log = new AlertLog(config, flight, method.toUpperCase(), alertMsg, flight.getPriceInr());
                alertLogRepository.save(log);

                if ("SMTP".equalsIgnoreCase(method) || "BOTH".equalsIgnoreCase(method)) {
                    sendEmailAlert(config, flight);
                }

                if ("TELEGRAM".equalsIgnoreCase(method) || "BOTH".equalsIgnoreCase(method)) {
                    sendTelegramAlert(config, flight);
                }
            } else {
                System.out.println("ℹ️ Alert already logged for " + flight.getFlightDate() + " in last 24h. Skipping alert to prevent spam.");
            }
        }
    }

    private void sendEmailAlert(SearchConfig config, PriceHistory flight) {
        try {
            String host = (config.getSmtpServer() != null && !config.getSmtpServer().isEmpty()) ? config.getSmtpServer() : defaultSmtpHost;
            int port = (config.getSmtpPort() != null && config.getSmtpPort() > 0) ? config.getSmtpPort() : defaultSmtpPort;
            String user = (config.getSmtpUser() != null && !config.getSmtpUser().isEmpty()) ? config.getSmtpUser() : defaultSmtpUser;
            String rawPass = (config.getSmtpPassEncrypted() != null && !config.getSmtpPassEncrypted().isEmpty())
                    ? EncryptionUtil.decrypt(config.getSmtpPassEncrypted(), secretKey) : defaultSmtpPass;
            String recipient = (config.getAlertRecipient() != null && !config.getAlertRecipient().isEmpty()) ? config.getAlertRecipient() : defaultRecipient;

            if (user == null || user.contains("your_email") || recipient == null || recipient.contains("example.com")) {
                System.out.println("⚠️ SMTP email credentials not configured for profile " + config.getId() + ". Skipping email alert.");
                return;
            }

            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost(host);
            mailSender.setPort(port);
            mailSender.setUsername(user);
            mailSender.setPassword(rawPass);

            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.timeout", "5000");
            props.put("mail.smtp.connectiontimeout", "5000");

            String bookingUrl = flight.getSourceUrl() != null ? flight.getSourceUrl() :
                    String.format("https://www.google.com/travel/flights?q=one-way+flights+from+%s+to+%s+on+%s",
                            config.getDepartureCity(), config.getArrivalCity(), flight.getFlightDate());

            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(user);
            mailMessage.setTo(recipient);
            mailMessage.setSubject("🚨 Flight Price Alert: " + config.getDepartureCity() + " → " + config.getArrivalCity() + " ₹" + flight.getPriceInr());

            String body = String.format("""
                    Great news! Flight price dropped below your target budget!
                    
                    Flight Details:
                    - Route: %s → %s
                    - Date: %s
                    - Airline: %s (%s)
                    - Departure: %s | Arrival: %s
                    - Price: ₹%s (Budget Target: ₹%d)
                    
                    🔗 Book Flight on Source Link:
                    %s
                    """,
                    config.getDepartureCity(), config.getArrivalCity(),
                    flight.getFlightDate(),
                    flight.getAirline(), flight.getFlightNumber(),
                    flight.getDepartureTime(), flight.getArrivalTime(),
                    flight.getPriceInr(), config.getBudgetThreshold(),
                    bookingUrl
            );

            mailMessage.setText(body);
            mailSender.send(mailMessage);
            System.out.println("✅ SMTP Email notification sent to " + recipient + " for profile #" + config.getId());
        } catch (Exception e) {
            System.err.println("❌ Failed to send SMTP alert for profile #" + config.getId() + ": " + e.getMessage());
        }
    }

    private void sendTelegramAlert(SearchConfig config, PriceHistory flight) {
        try {
            String token = (config.getTelegramBotTokenEncrypted() != null && !config.getTelegramBotTokenEncrypted().isEmpty())
                    ? EncryptionUtil.decrypt(config.getTelegramBotTokenEncrypted(), secretKey) : "";
            String chatId = config.getTelegramChatId();

            if (token.isEmpty() || chatId == null || chatId.isEmpty() || token.contains("your_telegram")) {
                System.out.println("⚠️ Telegram bot token or Chat ID not configured for profile #" + config.getId() + ". Skipping Telegram alert.");
                return;
            }

            String bookingUrl = flight.getSourceUrl() != null ? flight.getSourceUrl() :
                    String.format("https://www.google.com/travel/flights?q=one-way+flights+from+%s+to+%s+on+%s",
                            config.getDepartureCity(), config.getArrivalCity(), flight.getFlightDate());

            String messageText = String.format("""
                    🚨 *FLIGHT PRICE ALERT!*
                    
                    ✈️ *Route:* %s → %s
                    📅 *Date:* %s
                    🛩️ *Airline:* %s (%s)
                    ⏰ *Time:* %s → %s
                    💰 *Price:* ₹%s *(Budget: ₹%d)*
                    
                    [🔗 Book Source Deal](%s)
                    """,
                    config.getDepartureCity(), config.getArrivalCity(),
                    flight.getFlightDate(),
                    flight.getAirline(), flight.getFlightNumber(),
                    flight.getDepartureTime(), flight.getArrivalTime(),
                    flight.getPriceInr(), config.getBudgetThreshold(),
                    bookingUrl
            );

            String url = "https://api.telegram.org/bot" + token + "/sendMessage";
            String bodyData = "chat_id=" + URLEncoder.encode(chatId, StandardCharsets.UTF_8)
                    + "&text=" + URLEncoder.encode(messageText, StandardCharsets.UTF_8)
                    + "&parse_mode=Markdown";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(bodyData))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println("✅ Telegram alert sent to Chat ID " + chatId + " for profile #" + config.getId());
            } else {
                System.err.println("❌ Telegram API error (Status " + response.statusCode() + "): " + response.body());
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to send Telegram alert for profile #" + config.getId() + ": " + e.getMessage());
        }
    }

    public Map<String, Object> testSmtpConnection(String host, Integer port, String user, String pass, String recipient) {
        Map<String, Object> res = new java.util.HashMap<>();
        try {
            if (host == null || host.trim().isEmpty()) throw new IllegalArgumentException("SMTP Host is required.");
            if (user == null || user.trim().isEmpty()) throw new IllegalArgumentException("Sender Email is required.");
            if (pass == null || pass.trim().isEmpty()) throw new IllegalArgumentException("App Password is required.");
            if (recipient == null || recipient.trim().isEmpty()) throw new IllegalArgumentException("Alert Recipient Email is required.");

            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost(host.trim());
            mailSender.setPort(port != null && port > 0 ? port : 587);
            mailSender.setUsername(user.trim());
            mailSender.setPassword(pass.trim());

            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.timeout", "6000");
            props.put("mail.smtp.connectiontimeout", "6000");

            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(user.trim());
            mailMessage.setTo(recipient.trim());
            mailMessage.setSubject("✈️ Flight Tracker - SMTP Connection Test");
            mailMessage.setText("Hello,\n\nThis is a test message from your Flight Tracker system.\nYour SMTP email alert configuration is working correctly! 🎉\n\nServer: " + host + "\nUser: " + user);

            mailSender.send(mailMessage);
            res.put("success", true);
            res.put("message", "SMTP test email delivered successfully to " + recipient + "!");
        } catch (Exception e) {
            res.put("success", false);
            res.put("error", e.getMessage() != null ? e.getMessage() : e.toString());
        }
        return res;
    }

    public Map<String, Object> testTelegramConnection(String botToken, String chatId) {
        Map<String, Object> res = new java.util.HashMap<>();
        try {
            if (botToken == null || botToken.trim().isEmpty()) throw new IllegalArgumentException("Telegram Bot Token is required.");
            if (chatId == null || chatId.trim().isEmpty()) throw new IllegalArgumentException("Telegram Chat ID is required.");

            String messageText = "✈️ *Flight Tracker - Telegram Connection Test*\n\nYour Telegram bot configuration is working correctly! 🎉";

            String url = "https://api.telegram.org/bot" + botToken.trim() + "/sendMessage";
            String bodyData = "chat_id=" + URLEncoder.encode(chatId.trim(), StandardCharsets.UTF_8)
                    + "&text=" + URLEncoder.encode(messageText, StandardCharsets.UTF_8)
                    + "&parse_mode=Markdown";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(bodyData))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && response.body().contains("\"ok\":true")) {
                res.put("success", true);
                res.put("message", "Telegram test message sent successfully to Chat ID: " + chatId);
            } else {
                res.put("success", false);
                res.put("error", "Telegram API returned status " + response.statusCode() + ": " + response.body());
            }
        } catch (Exception e) {
            res.put("success", false);
            res.put("error", e.getMessage() != null ? e.getMessage() : e.toString());
        }
        return res;
    }
}
