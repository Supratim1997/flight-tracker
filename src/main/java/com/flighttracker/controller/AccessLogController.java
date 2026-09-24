package com.flighttracker.controller;

import com.flighttracker.entity.PriceAccessLog;
import com.flighttracker.repository.PriceAccessLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class AccessLogController {

    private final PriceAccessLogRepository accessLogRepository;

    public AccessLogController(PriceAccessLogRepository accessLogRepository) {
        this.accessLogRepository = accessLogRepository;
    }

    @GetMapping("/get-access-logs")
    public ResponseEntity<Map<String, Object>> getAccessLogs(@RequestParam(value = "config_id", defaultValue = "0") Long configId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<PriceAccessLog> logs;
            if (configId != null && configId > 0) {
                logs = accessLogRepository.findTop100BySearchConfigIdOrderByAccessedAtDesc(configId);
            } else {
                logs = accessLogRepository.findTop100ByOrderByAccessedAtDesc();
            }

            List<Map<String, Object>> formatted = new ArrayList<>();
            for (PriceAccessLog log : logs) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", log.getId());
                item.put("config_id", log.getSearchConfig().getId());
                item.put("flight_number", log.getFlightNumber());
                item.put("flight_date", log.getFlightDate().toString());
                item.put("airline", log.getAirline());
                item.put("source_name", log.getSourceName());
                item.put("accessed_url", log.getAccessedUrl());
                item.put("price_received", log.getPriceReceived().doubleValue());
                item.put("accessed_at", log.getAccessedAt().toString());
                item.put("departure_city", log.getSearchConfig().getDepartureCity());
                item.put("arrival_city", log.getSearchConfig().getArrivalCity());
                formatted.add(item);
            }

            response.put("success", true);
            response.put("data", formatted);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
