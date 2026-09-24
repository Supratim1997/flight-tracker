package com.flighttracker.service.provider;

import com.flighttracker.service.FlightScraperService.ScrapedFlight;

import java.time.LocalDate;
import java.util.List;

public interface FlightProvider {
    /**
     * Unique identifier for the platform (e.g. "Google Flights", "MakeMyTrip", "EaseMyTrip", etc.)
     */
    String getProviderName();

    /**
     * Whether this provider is enabled for live flight scanning
     */
    boolean isEnabled();

    /**
     * Search flights for a given origin, destination, target date, and flight preference.
     * All implementations MUST handle exceptions internally, return empty list on anti-bot/network failure,
     * and never throw unhandled exceptions to the caller.
     */
    List<ScrapedFlight> searchFlights(String dep, String arr, LocalDate flightDate, String flightType);
}
