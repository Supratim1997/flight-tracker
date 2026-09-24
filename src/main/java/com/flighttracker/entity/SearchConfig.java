package com.flighttracker.entity;

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
    private String flightType = "ALL";

    @Column(name = "active", nullable = false)
    private Integer active = 1;

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
        this.flightType = flightType != null ? flightType : "ALL";
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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
