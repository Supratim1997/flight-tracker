package com.flighttracker.service.provider;

import com.flighttracker.service.FlightScraperService;
import com.flighttracker.service.FlightScraperService.ScrapedFlight;
import com.flighttracker.util.AppConstants;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class GoogleFlightsProvider implements FlightProvider {

    private final FlightScraperService scraperService;

    public GoogleFlightsProvider(FlightScraperService scraperService) {
        this.scraperService = scraperService;
    }

    @Override
    public String getProviderName() {
        return AppConstants.PROVIDER_GOOGLE_FLIGHTS;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public List<ScrapedFlight> searchFlights(String dep, String arr, LocalDate flightDate, String flightType) {
        try {
            return scraperService.scrapeGoogleFlights(dep, arr, flightDate, flightType);
        } catch (Exception e) {
            System.err.println("⚠️ [GoogleFlightsProvider] Error: " + e.getMessage());
            return List.of();
        }
    }
}
