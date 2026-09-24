package com.flighttracker.service;

import com.flighttracker.entity.AlertLog;
import com.flighttracker.entity.PriceHistory;
import com.flighttracker.entity.SearchConfig;
import com.flighttracker.repository.AlertLogRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlertService {

    private final JavaMailSender mailSender;
    private final AlertLogRepository alertLogRepository;

    @Value("${app.alert.recipient:alert_recipient@example.com}")
    private String defaultRecipient;

    @Value("${spring.mail.username:your_email@gmail.com}")
    private String senderEmail;

    public AlertService(JavaMailSender mailSender, AlertLogRepository alertLogRepository) {
        this.mailSender = mailSender;
        this.alertLogRepository = alertLogRepository;
    }

    public void processPriceAlert(SearchConfig config, PriceHistory flight) {
        BigDecimal budget = BigDecimal.valueOf(config.getBudgetThreshold());
        if (flight.getPriceInr().compareTo(budget) <= 0) {
            LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
            List<AlertLog> recentAlerts = alertLogRepository.findRecentAlertsForDate(
                    config.getId(), cutoff, flight.getFlightDate().toString()
            );

            if (recentAlerts.isEmpty()) {
                String alertMsg = String.format("Price drop alert! %s %s on %s is now ₹%s",
                        flight.getAirline(), flight.getFlightNumber(), flight.getFlightDate(), flight.getPriceInr());

                AlertLog log = new AlertLog(config, flight, "SYSTEM_SPRING_BOOT", alertMsg, flight.getPriceInr());
                alertLogRepository.save(log);

                sendEmailAlert(config, flight);
            } else {
                System.out.println("ℹ️ Alert already logged for " + flight.getFlightDate() + " in last 24h. Skipping email to prevent spam.");
            }
        }
    }

    private void sendEmailAlert(SearchConfig config, PriceHistory flight) {
        try {
            if (senderEmail != null && !senderEmail.contains("your_email") && defaultRecipient != null) {
                String bookingUrl = flight.getSourceUrl() != null ? flight.getSourceUrl() :
                        String.format("https://www.google.com/travel/flights?q=one-way+flights+from+%s+to+%s+on+%s",
                                config.getDepartureCity(), config.getArrivalCity(), flight.getFlightDate());

                SimpleMailMessage mailMessage = new SimpleMailMessage();
                mailMessage.setFrom(senderEmail);
                mailMessage.setTo(defaultRecipient);
                mailMessage.setSubject("🚨 Flight Price Alert: " + config.getDepartureCity() + " to " + config.getArrivalCity() + " ₹" + flight.getPriceInr());

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
                System.out.println("✅ SMTP Email notification sent to " + defaultRecipient);
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to send SMTP alert: " + e.getMessage());
        }
    }
}
