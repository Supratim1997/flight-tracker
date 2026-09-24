package com.flighttracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flighttracker.util.AppConstants;
import org.springframework.stereotype.Service;

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
import java.util.*;

@Service
public class FlightScraperService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public FlightScraperService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(12))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public static class ScrapedFlight {
        public String airline;
        public String flightNumber;
        public LocalTime departureTime;
        public LocalTime arrivalTime;
        public BigDecimal price;
        public Boolean isDirect;
        public String stopsInfo;
        public String sourceUrl;
        public String sourceName;

        public ScrapedFlight(String airline, String flightNumber, LocalTime departureTime, LocalTime arrivalTime,
                             BigDecimal price, Boolean isDirect, String stopsInfo, String sourceUrl) {
            this(airline, flightNumber, departureTime, arrivalTime, price, isDirect, stopsInfo, sourceUrl, AppConstants.PROVIDER_GOOGLE_FLIGHTS);
        }

        public ScrapedFlight(String airline, String flightNumber, LocalTime departureTime, LocalTime arrivalTime,
                             BigDecimal price, Boolean isDirect, String stopsInfo, String sourceUrl, String sourceName) {
            this.airline = airline;
            this.flightNumber = flightNumber;
            this.departureTime = departureTime;
            this.arrivalTime = arrivalTime;
            this.price = price;
            this.isDirect = isDirect;
            this.stopsInfo = stopsInfo;
            this.sourceUrl = sourceUrl;
            this.sourceName = sourceName != null ? sourceName : AppConstants.PROVIDER_GOOGLE_FLIGHTS;
        }
    }

    public List<ScrapedFlight> scrapeGoogleFlights(String dep, String arr, LocalDate flightDate, String flightType) {
        String stopsParam = "";
        if (AppConstants.FLIGHT_TYPE_DIRECT.equalsIgnoreCase(flightType)) {
            stopsParam = "&stops=0";
        } else if (AppConstants.FLIGHT_TYPE_LAYOVER.equalsIgnoreCase(flightType)) {
            stopsParam = "&stops=1";
        }

        String queryStr = "one-way flights from " + dep.toUpperCase() + " to " + arr.toUpperCase() + " on " + flightDate.toString();
        String url = AppConstants.GOOGLE_FLIGHTS_SEARCH_URL + URLEncoder.encode(queryStr, StandardCharsets.UTF_8) + "&curr=INR" + stopsParam;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", AppConstants.SCRAPER_USER_AGENT)
                    .header("Accept-Language", AppConstants.SCRAPER_ACCEPT_LANGUAGE)
                    .timeout(Duration.ofSeconds(12))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                return Collections.emptyList();
            }

            String html = response.body();
            int pos = html.indexOf("key: 'ds:1'");
            if (pos == -1) {
                return Collections.emptyList();
            }

            int dataStart = html.indexOf("data:", pos) + 5;
            int dataEnd = html.indexOf("});</script>", dataStart);
            if (dataStart == -1 || dataEnd == -1 || dataEnd <= dataStart) {
                return Collections.emptyList();
            }

            String rawJson = html.substring(dataStart, dataEnd).trim();
            JsonNode rootNode = null;

            for (int i = rawJson.length(); i > Math.max(0, rawJson.length() - 500); i--) {
                try {
                    rootNode = objectMapper.readTree(rawJson.substring(0, i));
                    break;
                } catch (Exception ignored) {}
            }

            if (rootNode == null) {
                return Collections.emptyList();
            }

            List<JsonNode> rawItems = new ArrayList<>();
            findFlightNodes(rootNode, rawItems);

            List<ScrapedFlight> flights = new ArrayList<>();
            Set<String> seenKeys = new HashSet<>();

            for (JsonNode itemNode : rawItems) {
                if (!itemNode.isArray() || itemNode.size() < 2) continue;

                JsonNode priceArray = itemNode.get(1);
                if (!priceArray.isArray() || priceArray.size() == 0) continue;

                // Find the absolute minimum price among all booking options in priceArray
                double priceVal = Double.MAX_VALUE;
                for (JsonNode pElem : priceArray) {
                    if (pElem.isArray() && pElem.size() >= 2) {
                        for (int k = 0; k < pElem.size(); k++) {
                            if (pElem.get(k).isNumber()) {
                                double p = pElem.get(k).asDouble();
                                if (p >= 500 && p <= 500000 && p < priceVal) {
                                    priceVal = p;
                                }
                            }
                        }
                    }
                }

                if (priceVal == Double.MAX_VALUE) continue;
                BigDecimal price = BigDecimal.valueOf(priceVal);

                JsonNode legContainer = itemNode.get(0);
                if (!legContainer.isArray() || legContainer.size() < 3) continue;

                String carrier = legContainer.get(0).isTextual() ? legContainer.get(0).asText() : "";
                String airline = carrier;
                if (legContainer.get(1).isArray() && legContainer.get(1).size() > 0 && legContainer.get(1).get(0).isTextual()) {
                    airline = legContainer.get(1).get(0).asText();
                }

                JsonNode legsDetail = legContainer.get(2);
                if (!legsDetail.isArray() || legsDetail.size() == 0) continue;

                int numLegs = legsDetail.size();
                boolean isDirect = (numLegs == 1);

                JsonNode firstLeg = legsDetail.get(0);
                JsonNode lastLeg = legsDetail.get(numLegs - 1);
                if (!firstLeg.isArray()) continue;

                // Departure Time
                JsonNode depT = firstLeg.get(8);
                int depH = parseHour(depT);
                int depM = parseMinute(depT);
                LocalTime depTime = LocalTime.of(depH, depM, 0);

                // Arrival Time
                JsonNode arrT = lastLeg.get(10);
                int arrH = parseHour(arrT);
                int arrM = parseMinute(arrT);
                LocalTime arrTime = LocalTime.of(arrH, arrM, 0);

                // Extract exact real carrier flight number (e.g. AI-2951, 6E-6921, QP-1102)
                String flightNum = "";
                if (firstLeg.size() > 22 && firstLeg.get(22).isArray() && firstLeg.get(22).size() >= 2) {
                    JsonNode fCarrierNode = firstLeg.get(22).get(0);
                    JsonNode fCodeNode = firstLeg.get(22).get(1);
                    String fCarrier = (fCarrierNode != null && fCarrierNode.isTextual()) ? fCarrierNode.asText() : carrier;
                    String fCode = (fCodeNode != null && (fCodeNode.isTextual() || fCodeNode.isNumber())) ? fCodeNode.asText() : "";
                    if (!fCarrier.isEmpty() && !fCode.isEmpty()) {
                        flightNum = fCarrier + "-" + fCode;
                    }
                }
                if (flightNum.isEmpty()) {
                    flightNum = (carrier.isEmpty() ? "FL" : carrier) + "-" + (100 + Math.abs((airline + depTime).hashCode() % 899));
                }

                // Stops info
                String stopsInfo;
                if (isDirect) {
                    stopsInfo = AppConstants.DIRECT_STOPS_TEXT;
                } else {
                    String viaCity = (firstLeg.size() > 6 && firstLeg.get(6).isTextual()) ? firstLeg.get(6).asText() : "Layover";
                    stopsInfo = (numLegs - 1) + " Stop (" + viaCity + ")";
                }

                if (AppConstants.FLIGHT_TYPE_DIRECT.equalsIgnoreCase(flightType) && !isDirect) continue;
                if (AppConstants.FLIGHT_TYPE_LAYOVER.equalsIgnoreCase(flightType) && isDirect) continue;

                String key = airline + "|" + flightNum + "|" + depTime + "|" + priceVal;
                if (!seenKeys.contains(key)) {
                    seenKeys.add(key);
                    flights.add(new ScrapedFlight(airline, flightNum, depTime, arrTime, price, isDirect, stopsInfo, url));
                }
            }

            flights.sort(Comparator.comparing(f -> f.price));
            return flights.size() > 25 ? flights.subList(0, 25) : flights;

        } catch (Exception e) {
            System.err.println("⚠️ FlightScraperService warning: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private void findFlightNodes(JsonNode node, List<JsonNode> result) {
        if (node.isArray()) {
            if (node.size() >= 2 && node.get(1).isArray() && node.get(1).size() > 0) {
                for (JsonNode pNode : node.get(1)) {
                    if (pNode.isArray()) {
                        for (int k = 0; k < pNode.size(); k++) {
                            if (pNode.get(k).isNumber()) {
                                double val = pNode.get(k).asDouble();
                                if (val >= 500 && val <= 500000) {
                                    result.add(node);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            for (JsonNode child : node) {
                if (child.isArray()) {
                    findFlightNodes(child, result);
                }
            }
        }
    }

    private int parseHour(JsonNode timeNode) {
        if (timeNode != null && timeNode.isArray() && timeNode.size() > 0) {
            JsonNode hNode = timeNode.get(0);
            if (hNode != null && !hNode.isNull() && hNode.isNumber()) {
                return hNode.asInt();
            }
        }
        return 0; // Midnight 00:xx hour
    }

    private int parseMinute(JsonNode timeNode) {
        if (timeNode != null && timeNode.isArray() && timeNode.size() > 1) {
            JsonNode mNode = timeNode.get(1);
            if (mNode != null && !mNode.isNull() && mNode.isNumber()) {
                return mNode.asInt();
            }
        }
        return 0;
    }
}
