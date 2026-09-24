package com.flighttracker.entity;

import com.flighttracker.util.AppConstants;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "search_configs")
public class SearchConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "departure_city", nullable = false, length = 10)
    private String departureCity;

    @Column(name = "arrival_city", nullable = false, length = 10)
    private String arrivalCity;

    @Column(name = "preferred_date", nullable = false)
    private LocalDate preferredDate;

    @Column(name = "budget_threshold", nullable = false)
    private Integer budgetThreshold;

    @Column(name = "flight_type", length = 20)
    private String flightType = AppConstants.FLIGHT_TYPE_ALL;

    @Column(name = "active", nullable = false)
    private Integer active = 1;

    // Per-Profile Alert Notification Settings
    @Column(name = "alert_method", length = 20)
    private String alertMethod = AppConstants.ALERT_METHOD_NONE; // NONE, SMTP, TELEGRAM, BOTH

    @Column(name = "smtp_server", length = 100)
    private String smtpServer;

    @Column(name = "smtp_port")
    private Integer smtpPort = AppConstants.DEFAULT_SMTP_PORT;

    @Column(name = "smtp_user", length = 100)
    private String smtpUser;

    @Column(name = "smtp_pass_encrypted", length = 255)
    private String smtpPassEncrypted;

    @Column(name = "alert_recipient", length = 100)
    private String alertRecipient;

    @Column(name = "telegram_bot_token_encrypted", length = 255)
    private String telegramBotTokenEncrypted;

    @Column(name = "telegram_chat_id", length = 50)
    private String telegramChatId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public SearchConfig() {}

    public SearchConfig(String departureCity, String arrivalCity, LocalDate preferredDate, Integer budgetThreshold, String flightType, Integer active) {
        this.departureCity = departureCity;
        this.arrivalCity = arrivalCity;
        this.preferredDate = preferredDate;
        this.budgetThreshold = budgetThreshold;
        this.flightType = flightType != null ? flightType : AppConstants.FLIGHT_TYPE_ALL;
        this.active = active != null ? active : 1;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDepartureCity() { return departureCity; }
    public void setDepartureCity(String departureCity) { this.departureCity = departureCity; }

    public String getArrivalCity() { return arrivalCity; }
    public void setArrivalCity(String arrivalCity) { this.arrivalCity = arrivalCity; }

    public LocalDate getPreferredDate() { return preferredDate; }
    public void setPreferredDate(LocalDate preferredDate) { this.preferredDate = preferredDate; }

    public Integer getBudgetThreshold() { return budgetThreshold; }
    public void setBudgetThreshold(Integer budgetThreshold) { this.budgetThreshold = budgetThreshold; }

    public String getFlightType() { return flightType; }
    public void setFlightType(String flightType) { this.flightType = flightType; }

    public Integer getActive() { return active; }
    public void setActive(Integer active) { this.active = active; }

    public String getAlertMethod() { return alertMethod != null ? alertMethod : AppConstants.ALERT_METHOD_NONE; }
    public void setAlertMethod(String alertMethod) { this.alertMethod = alertMethod; }

    public String getSmtpServer() { return smtpServer; }
    public void setSmtpServer(String smtpServer) { this.smtpServer = smtpServer; }

    public Integer getSmtpPort() { return smtpPort; }
    public void setSmtpPort(Integer smtpPort) { this.smtpPort = smtpPort; }

    public String getSmtpUser() { return smtpUser; }
    public void setSmtpUser(String smtpUser) { this.smtpUser = smtpUser; }

    public String getSmtpPassEncrypted() { return smtpPassEncrypted; }
    public void setSmtpPassEncrypted(String smtpPassEncrypted) { this.smtpPassEncrypted = smtpPassEncrypted; }

    public String getAlertRecipient() { return alertRecipient; }
    public void setAlertRecipient(String alertRecipient) { this.alertRecipient = alertRecipient; }

    public String getTelegramBotTokenEncrypted() { return telegramBotTokenEncrypted; }
    public void setTelegramBotTokenEncrypted(String telegramBotTokenEncrypted) { this.telegramBotTokenEncrypted = telegramBotTokenEncrypted; }

    public String getTelegramChatId() { return telegramChatId; }
    public void setTelegramChatId(String telegramChatId) { this.telegramChatId = telegramChatId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
