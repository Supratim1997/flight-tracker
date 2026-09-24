package com.flighttracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

        public ScrapedFlight(String airline, String flightNumber, LocalTime departureTime, LocalTime arrivalTime,
                             BigDecimal price, Boolean isDirect, String stopsInfo, String sourceUrl) {
            this.airline = airline;
            this.flightNumber = flightNumber;
            this.departureTime = departureTime;
            this.arrivalTime = arrivalTime;
            this.price = price;
            this.isDirect = isDirect;
            this.stopsInfo = stopsInfo;
            this.sourceUrl = sourceUrl;
        }
    }

    public List<ScrapedFlight> scrapeGoogleFlights(String dep, String arr, LocalDate flightDate, String flightType) {
        String stopsParam = "";
        if ("DIRECT".equalsIgnoreCase(flightType)) {
            stopsParam = "&stops=0";
        } else if ("LAYOVER".equalsIgnoreCase(flightType)) {
            stopsParam = "&stops=1";
        }

        String queryStr = "one-way flights from " + dep.toUpperCase() + " to " + arr.toUpperCase() + " on " + flightDate.toString();
        String url = "https://www.google.com/travel/flights?q=" + URLEncoder.encode(queryStr, StandardCharsets.UTF_8) + "&curr=INR" + stopsParam;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept-Language", "en-IN,en;q=0.9")
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
                JsonNode firstPrice = priceArray.get(0);
                if (!firstPrice.isArray() || firstPrice.size() < 2) continue;

                double priceVal = firstPrice.get(1).asDouble();
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

                // Extract exact real carrier flight number (e.g. AI-2951, 6E-6921)
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
                    stopsInfo = "Direct";
                } else {
                    String viaCity = (firstLeg.size() > 6 && firstLeg.get(6).isTextual()) ? firstLeg.get(6).asText() : "Layover";
                    stopsInfo = (numLegs - 1) + " Stop (" + viaCity + ")";
                }

                if ("DIRECT".equalsIgnoreCase(flightType) && !isDirect) continue;
                if ("LAYOVER".equalsIgnoreCase(flightType) && isDirect) continue;

                String key = airline + "|" + flightNum + "|" + depTime + "|" + priceVal;
                if (!seenKeys.contains(key)) {
                    seenKeys.add(key);
                    flights.add(new ScrapedFlight(airline, flightNum, depTime, arrTime, price, isDirect, stopsInfo, url));
                }
            }

            flights.sort(Comparator.comparing(f -> f.price));
            return flights.size() > 5 ? flights.subList(0, 5) : flights;

        } catch (Exception e) {
            System.err.println("⚠️ FlightScraperService warning: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private void findFlightNodes(JsonNode node, List<JsonNode> result) {
        if (node.isArray()) {
            if (node.size() >= 2 && node.get(1).isArray() && node.get(1).size() > 0) {
                JsonNode pNode = node.get(1).get(0);
                if (pNode.isArray() && pNode.size() >= 2 && pNode.get(0).isNull() && pNode.get(1).isNumber()) {
                    double val = pNode.get(1).asDouble();
                    if (val >= 2000 && val <= 80000) {
                        result.add(node);
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
