package com.flighttracker.controller;

import com.flighttracker.entity.SearchConfig;
import com.flighttracker.repository.SearchConfigRepository;
import com.flighttracker.service.AlertService;
import com.flighttracker.util.EncryptionUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ConfigController {

    private final SearchConfigRepository configRepository;
    private final AlertService alertService;

    @Value("${app.security.secret-key:FlightTrackerSecretKey2026#SecureAES}")
    private String secretKey;

    public ConfigController(SearchConfigRepository configRepository, AlertService alertService) {
        this.configRepository = configRepository;
        this.alertService = alertService;
    }

    @GetMapping("/configs")
    public ResponseEntity<Map<String, Object>> getConfigs() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<SearchConfig> configs = configRepository.findAll();
            // Prepare clean payload (do not expose full encrypted password strings in list view)
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (SearchConfig c : configs) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", c.getId());
                map.put("departureCity", c.getDepartureCity());
                map.put("arrivalCity", c.getArrivalCity());
                map.put("preferredDate", c.getPreferredDate());
                map.put("budgetThreshold", c.getBudgetThreshold());
                map.put("flightType", c.getFlightType());
                map.put("active", c.getActive());
                map.put("alertMethod", c.getAlertMethod());
                map.put("smtpServer", c.getSmtpServer());
                map.put("smtpPort", c.getSmtpPort());
                map.put("smtpUser", c.getSmtpUser());
                map.put("alertRecipient", c.getAlertRecipient());
                map.put("telegramChatId", c.getTelegramChatId());
                map.put("hasSmtpPass", c.getSmtpPassEncrypted() != null && !c.getSmtpPassEncrypted().isEmpty());
                map.put("hasTelegramToken", c.getTelegramBotTokenEncrypted() != null && !c.getTelegramBotTokenEncrypted().isEmpty());
                resultList.add(map);
            }

            response.put("success", true);
            response.put("data", resultList);
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
            @RequestParam(value = "active", defaultValue = "1") Integer active,
            @RequestParam(value = "alert_method", defaultValue = "NONE") String alertMethod,
            @RequestParam(value = "smtp_server", required = false) String smtpServer,
            @RequestParam(value = "smtp_port", defaultValue = "587") Integer smtpPort,
            @RequestParam(value = "smtp_user", required = false) String smtpUser,
            @RequestParam(value = "smtp_pass", required = false) String smtpPass,
            @RequestParam(value = "alert_recipient", required = false) String alertRecipient,
            @RequestParam(value = "telegram_bot_token", required = false) String telegramBotToken,
            @RequestParam(value = "telegram_chat_id", required = false) String telegramChatId) {

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
            config.setAlertMethod(alertMethod != null ? alertMethod : "NONE");

            if (smtpServer != null) config.setSmtpServer(smtpServer.trim());
            if (smtpPort != null) config.setSmtpPort(smtpPort);
            if (smtpUser != null) config.setSmtpUser(smtpUser.trim());
            if (alertRecipient != null) config.setAlertRecipient(alertRecipient.trim());

            // Encrypt password if provided (or keep existing if blank during edit)
            if (smtpPass != null && !smtpPass.trim().isEmpty() && !smtpPass.equals("••••••••")) {
                config.setSmtpPassEncrypted(EncryptionUtil.encrypt(smtpPass.trim(), secretKey));
            }

            if (telegramChatId != null) config.setTelegramChatId(telegramChatId.trim());

            // Encrypt Telegram token if provided (or keep existing if blank during edit)
            if (telegramBotToken != null && !telegramBotToken.trim().isEmpty() && !telegramBotToken.equals("••••••••")) {
                config.setTelegramBotTokenEncrypted(EncryptionUtil.encrypt(telegramBotToken.trim(), secretKey));
            }

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

    @PostMapping("/test-profile-smtp")
    public ResponseEntity<Map<String, Object>> testProfileSmtp(
            @RequestParam(value = "id", defaultValue = "0") Long id,
            @RequestParam(value = "smtp_server", required = false) String smtpServer,
            @RequestParam(value = "smtp_port", defaultValue = "587") Integer smtpPort,
            @RequestParam(value = "smtp_user", required = false) String smtpUser,
            @RequestParam(value = "smtp_pass", required = false) String smtpPass,
            @RequestParam(value = "alert_recipient", required = false) String alertRecipient) {

        String passToUse = smtpPass;
        if ((passToUse == null || passToUse.trim().isEmpty() || passToUse.equals("••••••••")) && id != null && id > 0) {
            Optional<SearchConfig> opt = configRepository.findById(id);
            if (opt.isPresent() && opt.get().getSmtpPassEncrypted() != null) {
                passToUse = EncryptionUtil.decrypt(opt.get().getSmtpPassEncrypted(), secretKey);
            }
        }

        Map<String, Object> result = alertService.testSmtpConnection(smtpServer, smtpPort, smtpUser, passToUse, alertRecipient);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/test-profile-telegram")
    public ResponseEntity<Map<String, Object>> testProfileTelegram(
            @RequestParam(value = "id", defaultValue = "0") Long id,
            @RequestParam(value = "telegram_bot_token", required = false) String telegramBotToken,
            @RequestParam(value = "telegram_chat_id", required = false) String telegramChatId) {

        String tokenToUse = telegramBotToken;
        if ((tokenToUse == null || tokenToUse.trim().isEmpty() || tokenToUse.equals("••••••••")) && id != null && id > 0) {
            Optional<SearchConfig> opt = configRepository.findById(id);
            if (opt.isPresent() && opt.get().getTelegramBotTokenEncrypted() != null) {
                tokenToUse = EncryptionUtil.decrypt(opt.get().getTelegramBotTokenEncrypted(), secretKey);
            }
        }

        Map<String, Object> result = alertService.testTelegramConnection(tokenToUse, telegramChatId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/save-alert-settings")
    public ResponseEntity<Map<String, Object>> saveAlertSettings(
            @RequestParam("id") Long id,
            @RequestParam(value = "alert_method", defaultValue = "NONE") String alertMethod,
            @RequestParam(value = "smtp_server", required = false) String smtpServer,
            @RequestParam(value = "smtp_port", defaultValue = "587") Integer smtpPort,
            @RequestParam(value = "smtp_user", required = false) String smtpUser,
            @RequestParam(value = "smtp_pass", required = false) String smtpPass,
            @RequestParam(value = "alert_recipient", required = false) String alertRecipient,
            @RequestParam(value = "telegram_bot_token", required = false) String telegramBotToken,
            @RequestParam(value = "telegram_chat_id", required = false) String telegramChatId) {

        Map<String, Object> response = new HashMap<>();
        try {
            SearchConfig config = configRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Profile with ID " + id + " not found."));

            config.setAlertMethod(alertMethod != null ? alertMethod : "NONE");
            if (smtpServer != null) config.setSmtpServer(smtpServer.trim());
            if (smtpPort != null) config.setSmtpPort(smtpPort);
            if (smtpUser != null) config.setSmtpUser(smtpUser.trim());
            if (alertRecipient != null) config.setAlertRecipient(alertRecipient.trim());

            if (smtpPass != null && !smtpPass.trim().isEmpty() && !smtpPass.equals("••••••••")) {
                config.setSmtpPassEncrypted(EncryptionUtil.encrypt(smtpPass.trim(), secretKey));
            }

            if (telegramChatId != null) config.setTelegramChatId(telegramChatId.trim());

            if (telegramBotToken != null && !telegramBotToken.trim().isEmpty() && !telegramBotToken.equals("••••••••")) {
                config.setTelegramBotTokenEncrypted(EncryptionUtil.encrypt(telegramBotToken.trim(), secretKey));
            }

            configRepository.save(config);
            response.put("success", true);
            response.put("message", "Alert settings updated for profile #" + id);
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

