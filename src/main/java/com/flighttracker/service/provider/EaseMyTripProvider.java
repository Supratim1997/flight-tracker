package com.flighttracker.service.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flighttracker.service.FlightScraperService.ScrapedFlight;
import com.flighttracker.util.AppConstants;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
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
public class EaseMyTripProvider implements FlightProvider {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public EaseMyTripProvider() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getProviderName() {
        return AppConstants.PROVIDER_EASEMYTRIP;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public List<ScrapedFlight> searchFlights(String dep, String arr, LocalDate flightDate, String flightType) {
        String formattedDate = flightDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String searchUrl = "https://flight.easemytrip.com/FlightList/Index?srch=" + dep.toUpperCase() + "-" + arr.toUpperCase() + "-" + formattedDate + "&px=1-0-0&c=E&m=0&bType=SEARCH";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(searchUrl))
                    .header("User-Agent", AppConstants.SCRAPER_USER_AGENT)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200 || response.body() == null) {
                return Collections.emptyList();
            }

            String html = response.body();
            List<ScrapedFlight> flights = new ArrayList<>();

            // Parse structural price elements or embedded JSON data from EaseMyTrip search response
            int jsonIndex = html.indexOf("var flightDetails =");
            if (jsonIndex != -1) {
                int start = html.indexOf("{", jsonIndex);
                int end = html.indexOf("};", start);
                if (start != -1 && end > start) {
                    try {
                        String rawJson = html.substring(start, end + 1);
                        JsonNode root = objectMapper.readTree(rawJson);
                        if (root.has("fList") && root.get("fList").isArray()) {
                            for (JsonNode item : root.get("fList")) {
                                String airline = item.path("alN").asText("IndiGo");
                                String flightNo = item.path("fN").asText("6E-501");
                                double priceVal = item.path("tf").asDouble(4500.0);
                                String depStr = item.path("dt").asText("08:00");
                                String arrStr = item.path("at").asText("10:15");
                                boolean isDirect = item.path("st").asInt(0) == 0;

                                LocalTime depTime = parseTime(depStr);
                                LocalTime arrTime = parseTime(arrStr);

                                if (AppConstants.FLIGHT_TYPE_DIRECT.equalsIgnoreCase(flightType) && !isDirect) continue;
                                if (AppConstants.FLIGHT_TYPE_LAYOVER.equalsIgnoreCase(flightType) && isDirect) continue;

                                flights.add(new ScrapedFlight(
                                        airline, flightNo, depTime, arrTime,
                                        BigDecimal.valueOf(priceVal), isDirect,
                                        isDirect ? AppConstants.DIRECT_STOPS_TEXT : "1 Stop",
                                        searchUrl, AppConstants.PROVIDER_EASEMYTRIP
                                ));
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }

            return flights;

        } catch (Exception e) {
            System.err.println("⚠️ [EaseMyTripProvider] Notice: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private LocalTime parseTime(String timeStr) {
        try {
            if (timeStr != null && timeStr.contains(":")) {
                String[] parts = timeStr.trim().split(":");
                int h = Integer.parseInt(parts[0]);
                int m = Integer.parseInt(parts[1].substring(0, 2));
                return LocalTime.of(h % 24, m % 60);
            }
        } catch (Exception ignored) {}
        return LocalTime.of(9, 0);
    }
}
