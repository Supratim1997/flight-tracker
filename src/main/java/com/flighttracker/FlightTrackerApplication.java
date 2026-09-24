package com.flighttracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FlightTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlightTrackerApplication.class, args);
        System.out.println("=================================================");
        System.out.println("🚀 FlightTracker Spring Boot Application Started!");
        System.out.println("🌐 Open Dashboard: http://localhost:8080/");
        System.out.println("=================================================");
    }
}
