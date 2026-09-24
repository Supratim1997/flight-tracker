package com.flighttracker.service;

import com.flighttracker.entity.PriceAccessLog;
import com.flighttracker.entity.PriceHistory;
import com.flighttracker.entity.SearchConfig;
import com.flighttracker.repository.PriceAccessLogRepository;
import com.flighttracker.repository.PriceHistoryRepository;
import com.flighttracker.repository.SearchConfigRepository;
import com.flighttracker.util.AppConstants;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PriceTrackingService {

    private final SearchConfigRepository configRepository;
    private final PriceHistoryRepository historyRepository;
    private final PriceAccessLogRepository accessLogRepository;
    private final FlightScraperService scraperService;
    private final AlertService alertService;

    public PriceTrackingService(SearchConfigRepository configRepository,
                                PriceHistoryRepository historyRepository,
                                PriceAccessLogRepository accessLogRepository,
                                FlightScraperService scraperService,
                                AlertService alertService) {
        this.configRepository = configRepository;
        this.historyRepository = historyRepository;
        this.accessLogRepository = accessLogRepository;
        this.scraperService = scraperService;
        this.alertService = alertService;
    }

    @Transactional
    public void runFullScrapeCheck() {
        List<SearchConfig> activeConfigs = configRepository.findByActive(1);
        if (activeConfigs.isEmpty()) {
            System.out.println("No active tracking configurations found.");
            return;
        }

        for (SearchConfig config : activeConfigs) {
            scrapeProfile(config);
        }
    }

    @Transactional
    public void scrapeProfile(SearchConfig config) {
        System.out.println("Processing config " + config.getId() + ": " + config.getDepartureCity() + " to " +
                config.getArrivalCity() + " (Target: " + config.getPreferredDate() + " | Type: " + config.getFlightType() + ")");

        historyRepository.deleteBySearchConfigId(config.getId());

        LocalDate targetDate = config.getPreferredDate();

        // 11-day window (-5 to +5)
        for (int i = -5; i <= 5; i++) {
            LocalDate currentDate = targetDate.plusDays(i);
            List<FlightScraperService.ScrapedFlight> scrapedFlights = scraperService.scrapeGoogleFlights(
                    config.getDepartureCity(), config.getArrivalCity(), currentDate, config.getFlightType()
            );

            for (FlightScraperService.ScrapedFlight f : scrapedFlights) {
                PriceHistory history = new PriceHistory(
                        config, currentDate, f.airline, f.flightNumber, f.departureTime, f.arrivalTime,
                        f.price, f.isDirect, f.stopsInfo, f.sourceUrl
                );
                PriceHistory savedHistory = historyRepository.save(history);

                PriceAccessLog accessLog = new PriceAccessLog(
                        config, f.flightNumber, currentDate, f.airline, AppConstants.SCRAPER_SOURCE_NAME,
                        f.sourceUrl, f.price
                );
                accessLogRepository.save(accessLog);

                alertService.processPriceAlert(config, savedHistory);
            }
        }
    }
}
