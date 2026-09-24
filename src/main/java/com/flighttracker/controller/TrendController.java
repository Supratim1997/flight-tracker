package com.flighttracker.controller;

import com.flighttracker.entity.PriceHistory;
import com.flighttracker.entity.SearchConfig;
import com.flighttracker.repository.PriceHistoryRepository;
import com.flighttracker.repository.SearchConfigRepository;
import com.flighttracker.util.AppConstants;
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
            response.put(AppConstants.KEY_SUCCESS, false);
            response.put(AppConstants.KEY_ERROR, "Invalid config ID");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Optional<SearchConfig> configOpt = configRepository.findById(configId);
            if (configOpt.isEmpty()) {
                response.put(AppConstants.KEY_SUCCESS, false);
                response.put(AppConstants.KEY_ERROR, "Config not found");
                return ResponseEntity.status(404).body(response);
            }

            SearchConfig config = configOpt.get();
            List<PriceHistory> history = historyRepository.findBySearchConfigIdOrderByFlightDateAscPriceInrAsc(configId);
            List<Map<String, Object>> dailyMin = historyRepository.findDailyMinimumsByConfigId(configId);

            // Format history data items for JSON response
            List<Map<String, Object>> formattedData = new ArrayList<>();
            for (PriceHistory ph : history) {
                Map<String, Object> item = new HashMap<>();
                item.put(AppConstants.KEY_ID, ph.getId());
                item.put(AppConstants.KEY_FLIGHT_DATE, ph.getFlightDate().toString());
                item.put(AppConstants.KEY_MIN_PRICE, ph.getPriceInr().doubleValue());
                item.put(AppConstants.KEY_AIRLINE, ph.getAirline());
                item.put(AppConstants.KEY_FLIGHT_NUMBER, ph.getFlightNumber());
                item.put(AppConstants.KEY_DEPARTURE_TIME, ph.getDepartureTime().toString());
                item.put(AppConstants.KEY_ARRIVAL_TIME, ph.getArrivalTime().toString());
                item.put(AppConstants.KEY_IS_DIRECT, ph.getIsDirect() ? 1 : 0);
                item.put(AppConstants.KEY_STOPS_INFO, ph.getStopsInfo());
                item.put(AppConstants.KEY_SOURCE_URL, ph.getSourceUrl());
                formattedData.add(item);
            }

            // Format daily minimums items for chart
            List<Map<String, Object>> formattedDailyMin = new ArrayList<>();
            for (Map<String, Object> dm : dailyMin) {
                Map<String, Object> item = new HashMap<>();
                item.put(AppConstants.KEY_FLIGHT_DATE, dm.get(AppConstants.MAP_KEY_FLIGHT_DATE).toString());
                item.put(AppConstants.KEY_MIN_PRICE, dm.get(AppConstants.MAP_KEY_MIN_PRICE));
                formattedDailyMin.add(item);
            }

            response.put(AppConstants.KEY_SUCCESS, true);
            response.put(AppConstants.KEY_DATA, formattedData);
            response.put(AppConstants.KEY_DAILY_MIN, formattedDailyMin);
            response.put(AppConstants.KEY_DEPARTURE_CITY, config.getDepartureCity());
            response.put(AppConstants.KEY_ARRIVAL_CITY, config.getArrivalCity());
            response.put(AppConstants.KEY_BUDGET_THRESHOLD, config.getBudgetThreshold());
            response.put(AppConstants.KEY_PREFERRED_DATE, config.getPreferredDate().toString());
            response.put(AppConstants.KEY_FLIGHT_TYPE, config.getFlightType());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put(AppConstants.KEY_SUCCESS, false);
            response.put(AppConstants.KEY_ERROR, e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
