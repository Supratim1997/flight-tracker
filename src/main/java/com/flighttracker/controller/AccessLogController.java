package com.flighttracker.controller;

import com.flighttracker.entity.PriceAccessLog;
import com.flighttracker.repository.PriceAccessLogRepository;
import com.flighttracker.util.AppConstants;
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
                item.put(AppConstants.KEY_ID, log.getId());
                item.put(AppConstants.KEY_CONFIG_ID, log.getSearchConfig().getId());
                item.put(AppConstants.KEY_FLIGHT_NUMBER, log.getFlightNumber());
                item.put(AppConstants.KEY_FLIGHT_DATE, log.getFlightDate().toString());
                item.put(AppConstants.KEY_AIRLINE, log.getAirline());
                item.put(AppConstants.KEY_SOURCE_NAME, log.getSourceName());
                item.put(AppConstants.KEY_ACCESSED_URL, log.getAccessedUrl());
                item.put(AppConstants.KEY_PRICE_RECEIVED, log.getPriceReceived().doubleValue());
                item.put(AppConstants.KEY_ACCESSED_AT, log.getAccessedAt().toString());
                item.put(AppConstants.KEY_DEPARTURE_CITY, log.getSearchConfig().getDepartureCity());
                item.put(AppConstants.KEY_ARRIVAL_CITY, log.getSearchConfig().getArrivalCity());
                formatted.add(item);
            }

            response.put(AppConstants.KEY_SUCCESS, true);
            response.put(AppConstants.KEY_DATA, formatted);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put(AppConstants.KEY_SUCCESS, false);
            response.put(AppConstants.KEY_ERROR, e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
