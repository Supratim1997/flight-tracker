package com.flighttracker.service.provider;

import com.flighttracker.service.FlightScraperService.ScrapedFlight;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;

@Service
public class MultiProviderScraperManager {

    private final List<FlightProvider> providers;
    private final ExecutorService executorService;

    public MultiProviderScraperManager(List<FlightProvider> providers) {
        this.providers = providers;
        this.executorService = Executors.newFixedThreadPool(16);
    }

    public List<FlightProvider> getProviders() {
        return Collections.unmodifiableList(providers);
    }

    /**
     * Executes flight search across all registered providers in parallel with error isolation,
     * non-blocking timeouts, and multi-OTA flight deduplication.
     */
    public List<ScrapedFlight> searchAllProviders(String dep, String arr, LocalDate flightDate, String flightType) {
        List<CompletableFuture<List<ScrapedFlight>>> futures = new ArrayList<>();

        for (FlightProvider provider : providers) {
            if (!provider.isEnabled()) continue;
            List<ScrapedFlight> defaultFallback = Collections.emptyList();
            CompletableFuture<List<ScrapedFlight>> future = CompletableFuture.supplyAsync(() -> {
                try {
                    List<ScrapedFlight> res = provider.searchFlights(dep, arr, flightDate, flightType);
                    return res != null ? res : defaultFallback;
                } catch (Exception e) {
                    return defaultFallback;
                }
            }, executorService).completeOnTimeout(defaultFallback, 4, TimeUnit.SECONDS);

            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<ScrapedFlight> allFlights = new ArrayList<>();
        Set<String> uniqueKeys = new HashSet<>();

        for (CompletableFuture<List<ScrapedFlight>> f : futures) {
            try {
                List<ScrapedFlight> providerFlights = f.get();
                if (providerFlights != null && !providerFlights.isEmpty()) {
                    for (ScrapedFlight flight : providerFlights) {
                        String key = flight.sourceName + "|" + flight.airline + "|" + flight.flightNumber + "|" + flight.departureTime + "|" + flight.price;
                        if (!uniqueKeys.contains(key)) {
                            uniqueKeys.add(key);
                            allFlights.add(flight);
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        allFlights.sort(Comparator.comparing(flight -> flight.price));
        return allFlights;
    }
}
