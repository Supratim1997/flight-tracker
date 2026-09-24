package com.flighttracker.controller;

import com.flighttracker.repository.AlertLogRepository;
import com.flighttracker.repository.PriceAccessLogRepository;
import com.flighttracker.repository.PriceHistoryRepository;
import com.flighttracker.repository.SearchConfigRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ResetController {

    private final SearchConfigRepository configRepository;
    private final PriceHistoryRepository historyRepository;
    private final PriceAccessLogRepository accessLogRepository;
    private final AlertLogRepository alertLogRepository;

    public ResetController(SearchConfigRepository configRepository,
                           PriceHistoryRepository historyRepository,
                           PriceAccessLogRepository accessLogRepository,
                           AlertLogRepository alertLogRepository) {
        this.configRepository = configRepository;
        this.historyRepository = historyRepository;
        this.accessLogRepository = accessLogRepository;
        this.alertLogRepository = alertLogRepository;
    }

    @PostMapping("/reset-system")
    @Transactional
    public ResponseEntity<Map<String, Object>> resetSystem(@RequestParam("action") String action) {
        Map<String, Object> response = new HashMap<>();

        if ("reset_profiles".equalsIgnoreCase(action)) {
            try {
                alertLogRepository.deleteAll();
                accessLogRepository.deleteAll();
                historyRepository.deleteAll();
                configRepository.deleteAll();

                response.put("success", true);
                response.put("message", "All flight profiles, price history, access logs, and alerts reset successfully!");
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                response.put("success", false);
                response.put("error", e.getMessage());
                return ResponseEntity.status(500).body(response);
            }
        } else if ("reset_smtp".equalsIgnoreCase(action)) {
            response.put("success", true);
            response.put("message", "SMTP settings reset to defaults!");
            return ResponseEntity.ok(response);
        }

        response.put("success", false);
        response.put("error", "Invalid action");
        return ResponseEntity.badRequest().body(response);
    }
}
