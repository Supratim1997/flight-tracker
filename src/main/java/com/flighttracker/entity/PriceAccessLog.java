package com.flighttracker.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_access_logs")
public class PriceAccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "config_id", nullable = false)
    private SearchConfig searchConfig;

    @Column(name = "flight_number", nullable = false, length = 50)
    private String flightNumber;

    @Column(name = "flight_date", nullable = false)
    private LocalDate flightDate;

    @Column(name = "airline", nullable = false, length = 100)
    private String airline;

    @Column(name = "source_name", length = 100)
    private String sourceName = "Google Flights Stream";

    @Column(name = "accessed_url", nullable = false, columnDefinition = "TEXT")
    private String accessedUrl;

    @Column(name = "price_received", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceReceived;

    @Column(name = "accessed_at")
    private LocalDateTime accessedAt = LocalDateTime.now();

    public PriceAccessLog() {}

    public PriceAccessLog(SearchConfig searchConfig, String flightNumber, LocalDate flightDate, 
                          String airline, String sourceName, String accessedUrl, BigDecimal priceReceived) {
        this.searchConfig = searchConfig;
        this.flightNumber = flightNumber;
        this.flightDate = flightDate;
        this.airline = airline;
        this.sourceName = sourceName != null ? sourceName : "Google Flights Stream";
        this.accessedUrl = accessedUrl;
        this.priceReceived = priceReceived;
        this.accessedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public SearchConfig getSearchConfig() { return searchConfig; }
    public void setSearchConfig(SearchConfig searchConfig) { this.searchConfig = searchConfig; }

    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

    public LocalDate getFlightDate() { return flightDate; }
    public void setFlightDate(LocalDate flightDate) { this.flightDate = flightDate; }

    public String getAirline() { return airline; }
    public void setAirline(String airline) { this.airline = airline; }

    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }

    public String getAccessedUrl() { return accessedUrl; }
    public void setAccessedUrl(String accessedUrl) { this.accessedUrl = accessedUrl; }

    public BigDecimal getPriceReceived() { return priceReceived; }
    public void setPriceReceived(BigDecimal priceReceived) { this.priceReceived = priceReceived; }

    public LocalDateTime getAccessedAt() { return accessedAt; }
    public void setAccessedAt(LocalDateTime accessedAt) { this.accessedAt = accessedAt; }
}
