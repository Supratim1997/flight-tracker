package com.flighttracker.controller;

import com.flighttracker.entity.PriceHistory;
import com.flighttracker.entity.SearchConfig;
import com.flighttracker.repository.PriceHistoryRepository;
import com.flighttracker.repository.SearchConfigRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class TrendController {

    private final SearchConfigRepository configRepository;
    private final PriceHistoryRepository historyRepository;

    public TrendController(SearchConfigRepository configRepository, PriceHistoryRepository historyRepository) {
        this.configRepository = configRepository;
        this.historyRepository = historyRepository;
    }

    @GetMapping("/get-trend-data")
    public ResponseEntity<Map<String, Object>> getTrendData(@RequestParam("config_id") Long configId) {
        Map<String, Object> response = new HashMap<>();
        if (configId == null || configId <= 0) {
            response.put("success", false);
            response.put("error", "Invalid config ID");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Optional<SearchConfig> configOpt = configRepository.findById(configId);
            if (configOpt.isEmpty()) {
                response.put("success", false);
                response.put("error", "Config not found");
                return ResponseEntity.status(404).body(response);
            }

            SearchConfig config = configOpt.get();
            List<PriceHistory> history = historyRepository.findBySearchConfigIdOrderByFlightDateAscPriceInrAsc(configId);
            List<Map<String, Object>> dailyMin = historyRepository.findDailyMinimumsByConfigId(configId);

            // Format history data items for JSON response
            List<Map<String, Object>> formattedData = new ArrayList<>();
            for (PriceHistory ph : history) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", ph.getId());
                item.put("flight_date", ph.getFlightDate().toString());
                item.put("min_price", ph.getPriceInr().doubleValue());
                item.put("airline", ph.getAirline());
                item.put("flight_number", ph.getFlightNumber());
                item.put("departure_time", ph.getDepartureTime().toString());
                item.put("arrival_time", ph.getArrivalTime().toString());
                item.put("is_direct", ph.getIsDirect() ? 1 : 0);
                item.put("stops_info", ph.getStopsInfo());
                item.put("source_url", ph.getSourceUrl());
                formattedData.add(item);
            }

            // Format daily minimums items for chart
            List<Map<String, Object>> formattedDailyMin = new ArrayList<>();
            for (Map<String, Object> dm : dailyMin) {
                Map<String, Object> item = new HashMap<>();
                item.put("flight_date", dm.get("flightDate").toString());
                item.put("min_price", dm.get("minPrice"));
                formattedDailyMin.add(item);
            }

            response.put("success", true);
            response.put("data", formattedData);
            response.put("daily_min", formattedDailyMin);
            response.put("departure_city", config.getDepartureCity());
            response.put("arrival_city", config.getArrivalCity());
            response.put("budget_threshold", config.getBudgetThreshold());
            response.put("preferred_date", config.getPreferredDate().toString());
            response.put("flight_type", config.getFlightType());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
