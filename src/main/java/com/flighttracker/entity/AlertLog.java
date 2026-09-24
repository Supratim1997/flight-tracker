package com.flighttracker.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "alert_logs")
public class AlertLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "config_id", nullable = false)
    private SearchConfig searchConfig;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", nullable = false)
    private PriceHistory priceHistory;

    @Column(name = "alert_type", nullable = false, length = 50)
    private String alertType;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "triggered_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal triggeredPrice;

    @Column(name = "sent_at")
    private LocalDateTime sentAt = LocalDateTime.now();

    public AlertLog() {}

    public AlertLog(SearchConfig searchConfig, PriceHistory priceHistory, String alertType, String message, BigDecimal triggeredPrice) {
        this.searchConfig = searchConfig;
        this.priceHistory = priceHistory;
        this.alertType = alertType;
        this.message = message;
        this.triggeredPrice = triggeredPrice;
        this.sentAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public SearchConfig getSearchConfig() { return searchConfig; }
    public void setSearchConfig(SearchConfig searchConfig) { this.searchConfig = searchConfig; }

    public PriceHistory getPriceHistory() { return priceHistory; }
    public void setPriceHistory(PriceHistory priceHistory) { this.priceHistory = priceHistory; }

    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public BigDecimal getTriggeredPrice() { return triggeredPrice; }
    public void setTriggeredPrice(BigDecimal triggeredPrice) { this.triggeredPrice = triggeredPrice; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}
