package com.flighttracker.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_history")
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "config_id", nullable = false)
    private SearchConfig searchConfig;

    @Column(name = "flight_date", nullable = false)
    private LocalDate flightDate;

    @Column(name = "airline", nullable = false, length = 100)
    private String airline;

    @Column(name = "flight_number", nullable = false, length = 20)
    private String flightNumber;

    @Column(name = "departure_time", nullable = false)
    private LocalTime departureTime;

    @Column(name = "arrival_time", nullable = false)
    private LocalTime arrivalTime;

    @Column(name = "price_inr", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceInr;

    @Column(name = "is_direct")
    private Boolean isDirect = true;

    @Column(name = "stops_info", length = 50)
    private String stopsInfo = "Direct";

    @Column(name = "source_url", columnDefinition = "TEXT")
    private String sourceUrl;

    @Column(name = "scraped_at")
    private LocalDateTime scrapedAt = LocalDateTime.now();

    public PriceHistory() {}

    public PriceHistory(SearchConfig searchConfig, LocalDate flightDate, String airline, String flightNumber, 
                        LocalTime departureTime, LocalTime arrivalTime, BigDecimal priceInr, 
                        Boolean isDirect, String stopsInfo, String sourceUrl) {
        this.searchConfig = searchConfig;
        this.flightDate = flightDate;
        this.airline = airline;
        this.flightNumber = flightNumber;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.priceInr = priceInr;
        this.isDirect = isDirect != null ? isDirect : true;
        this.stopsInfo = stopsInfo != null ? stopsInfo : "Direct";
        this.sourceUrl = sourceUrl;
        this.scrapedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public SearchConfig getSearchConfig() { return searchConfig; }
    public void setSearchConfig(SearchConfig searchConfig) { this.searchConfig = searchConfig; }

    public LocalDate getFlightDate() { return flightDate; }
    public void setFlightDate(LocalDate flightDate) { this.flightDate = flightDate; }

    public String getAirline() { return airline; }
    public void setAirline(String airline) { this.airline = airline; }

    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

    public LocalTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalTime departureTime) { this.departureTime = departureTime; }

    public LocalTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalTime arrivalTime) { this.arrivalTime = arrivalTime; }

    public BigDecimal getPriceInr() { return priceInr; }
    public void setPriceInr(BigDecimal priceInr) { this.priceInr = priceInr; }

    public Boolean getIsDirect() { return isDirect; }
    public void setIsDirect(Boolean isDirect) { this.isDirect = isDirect; }

    public String getStopsInfo() { return stopsInfo; }
    public void setStopsInfo(String stopsInfo) { this.stopsInfo = stopsInfo; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public LocalDateTime getScrapedAt() { return scrapedAt; }
    public void setScrapedAt(LocalDateTime scrapedAt) { this.scrapedAt = scrapedAt; }
}
