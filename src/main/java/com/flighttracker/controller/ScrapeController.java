package com.flighttracker.controller;

import com.flighttracker.service.PriceTrackingService;
import com.flighttracker.util.AppConstants;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ScrapeController {

    private final PriceTrackingService priceTrackingService;

    public ScrapeController(PriceTrackingService priceTrackingService) {
        this.priceTrackingService = priceTrackingService;
    }

    @GetMapping("/trigger-scrape")
    public ResponseEntity<Map<String, Object>> triggerScrape() {
        Map<String, Object> response = new HashMap<>();
        try {
            priceTrackingService.runFullScrapeCheck();
            response.put(AppConstants.KEY_SUCCESS, true);
            response.put(AppConstants.KEY_OUTPUT, "Manual scrape completed successfully via Spring Boot engine.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put(AppConstants.KEY_SUCCESS, false);
            response.put(AppConstants.KEY_ERROR, e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
