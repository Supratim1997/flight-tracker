package com.flighttracker.controller;

import com.flighttracker.entity.SearchConfig;
import com.flighttracker.repository.SearchConfigRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ConfigController {

    private final SearchConfigRepository configRepository;

    public ConfigController(SearchConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @GetMapping("/configs")
    public ResponseEntity<Map<String, Object>> getConfigs() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<SearchConfig> configs = configRepository.findAll();
            response.put("success", true);
            response.put("data", configs);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/save-config")
    public ResponseEntity<Map<String, Object>> saveConfig(
            @RequestParam(value = "id", defaultValue = "0") Long id,
            @RequestParam("departure_city") String departureCity,
            @RequestParam("arrival_city") String arrivalCity,
            @RequestParam("preferred_date") String preferredDate,
            @RequestParam("budget_threshold") Integer budgetThreshold,
            @RequestParam(value = "flight_type", defaultValue = "ALL") String flightType,
            @RequestParam(value = "active", defaultValue = "1") Integer active) {

        Map<String, Object> response = new HashMap<>();
        try {
            SearchConfig config;
            if (id != null && id > 0) {
                config = configRepository.findById(id).orElse(new SearchConfig());
            } else {
                config = new SearchConfig();
            }

            config.setDepartureCity(departureCity.trim().toUpperCase());
            config.setArrivalCity(arrivalCity.trim().toUpperCase());
            config.setPreferredDate(LocalDate.parse(preferredDate));
            config.setBudgetThreshold(budgetThreshold);
            config.setFlightType(flightType);
            config.setActive(active);

            SearchConfig saved = configRepository.save(config);
            response.put("success", true);
            response.put("id", saved.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }

    @PostMapping("/delete-config")
    public ResponseEntity<Map<String, Object>> deleteConfig(@RequestParam("id") Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            configRepository.deleteById(id);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
