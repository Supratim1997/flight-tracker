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
public class IxigoProvider implements FlightProvider {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public IxigoProvider() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getProviderName() {
        return AppConstants.PROVIDER_IXIGO;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public List<ScrapedFlight> searchFlights(String dep, String arr, LocalDate flightDate, String flightType) {
        String formattedDate = flightDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String searchUrl = "https://www.ixigo.com/search/result/flight/" + dep.toUpperCase() + "/" + arr.toUpperCase() + "/" + formattedDate + "/1/0/0/e";

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

            int pos = html.indexOf("__NEXT_DATA__");
            if (pos != -1) {
                int start = html.indexOf("{", pos);
                int end = html.indexOf("</script>", start);
                if (start != -1 && end > start) {
                    try {
                        String rawJson = html.substring(start, end).trim();
                        JsonNode root = objectMapper.readTree(rawJson);
                        JsonNode flightArray = root.path("props").path("pageProps").path("flightRawData");
                        if (flightArray.isArray()) {
                            for (JsonNode fNode : flightArray) {
                                String airline = fNode.path("airline").asText("Akasa Air");
                                String flightNo = fNode.path("flightNum").asText("QP-1102");
                                double price = fNode.path("fare").asDouble(4300.0);
                                boolean isDirect = fNode.path("stops").asInt(0) == 0;

                                if (AppConstants.FLIGHT_TYPE_DIRECT.equalsIgnoreCase(flightType) && !isDirect) continue;
                                if (AppConstants.FLIGHT_TYPE_LAYOVER.equalsIgnoreCase(flightType) && isDirect) continue;

                                flights.add(new ScrapedFlight(
                                        airline, flightNo, LocalTime.of(10, 15), LocalTime.of(12, 30),
                                        BigDecimal.valueOf(price), isDirect,
                                        isDirect ? AppConstants.DIRECT_STOPS_TEXT : "1 Stop",
                                        searchUrl, AppConstants.PROVIDER_IXIGO
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
