package com.flighttracker.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ScheduledScraperService {

    private final PriceTrackingService priceTrackingService;

    public ScheduledScraperService(PriceTrackingService priceTrackingService) {
        this.priceTrackingService = priceTrackingService;
    }

    // Runs every 12 hours (43,200,000 ms), initial delay 10s after startup
    @Scheduled(fixedRate = 43200000, initialDelay = 10000)
    public void runScheduledScrape() {
        System.out.println("[" + LocalDateTime.now() + "] Executing scheduled background flight scrape...");
        try {
            priceTrackingService.runFullScrapeCheck();
        } catch (Exception e) {
            System.err.println("❌ Scheduled background scrape error: " + e.getMessage());
        }
    }
}
