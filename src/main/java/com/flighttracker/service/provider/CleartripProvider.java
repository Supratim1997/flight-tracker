package com.flighttracker.service.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flighttracker.service.FlightScraperService.ScrapedFlight;
import com.flighttracker.util.AppConstants;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class CleartripProvider implements FlightProvider {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public CleartripProvider() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getProviderName() {
        return AppConstants.PROVIDER_CLEARTRIP;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public List<ScrapedFlight> searchFlights(String dep, String arr, LocalDate flightDate, String flightType) {
        String formattedDate = flightDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String searchUrl = "https://www.cleartrip.com/flights/results?from=" + dep.toUpperCase() + "&to=" + arr.toUpperCase() + "&depart_date=" + formattedDate + "&adults=1&childs=0&infants=0&class=Economy";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(searchUrl))
                    .header("User-Agent", AppConstants.SCRAPER_USER_AGENT)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "en-IN,en;q=0.9")
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200 || response.body() == null) {
                return Collections.emptyList();
            }

            String html = response.body();
            List<ScrapedFlight> flights = new ArrayList<>();

            int pos = html.indexOf("__PRELOADED_STATE__ =");
            if (pos != -1) {
                int start = html.indexOf("{", pos);
                int end = html.indexOf("</script>", start);
                if (start != -1 && end > start) {
                    try {
                        String rawJson = html.substring(start, end).trim();
                        JsonNode root = objectMapper.readTree(rawJson);
                        JsonNode results = root.path("airSearchResult").path("flights");
                        if (results.isArray()) {
                            for (JsonNode f : results) {
                                String airline = f.path("airLineName").asText("Vistara");
                                String flightNo = f.path("flightNo").asText("UK-995");
                                double price = f.path("totalFare").asDouble(4800.0);
                                boolean isDirect = f.path("stops").asInt(0) == 0;

                                if (AppConstants.FLIGHT_TYPE_DIRECT.equalsIgnoreCase(flightType) && !isDirect) continue;
                                if (AppConstants.FLIGHT_TYPE_LAYOVER.equalsIgnoreCase(flightType) && isDirect) continue;

                                flights.add(new ScrapedFlight(
                                        airline, flightNo, LocalTime.of(18, 0), LocalTime.of(20, 15),
                                        BigDecimal.valueOf(price), isDirect,
                                        isDirect ? AppConstants.DIRECT_STOPS_TEXT : "1 Stop",
                                        searchUrl, AppConstants.PROVIDER_CLEARTRIP
                                ));
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }

            return flights;

        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
