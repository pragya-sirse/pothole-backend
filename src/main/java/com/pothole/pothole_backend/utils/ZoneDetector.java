package com.pothole.pothole_backend.utils;

import org.springframework.stereotype.Component;

@Component
public class ZoneDetector {

    // In production: use GIS polygon matching
    // For now: returns zone based on rough lat/lng ranges for Indore, Bhopal, Vidisha
    public String detectArea(Double lat, Double lng) {
        // Indore roughly: 22.6-22.8 N, 75.8-75.9 E
        if (lat >= 22.6 && lat <= 22.8 && lng >= 75.8 && lng <= 75.9) {
            return "Indore";
        }
        // Bhopal roughly: 23.2-23.3 N, 77.3-77.5 E
        if (lat >= 23.2 && lat <= 23.3 && lng >= 77.3 && lng <= 77.5) {
            return "Bhopal";
        }
        // Vidisha roughly: 23.5-23.6 N, 77.8-77.9 E
        if (lat >= 23.5 && lat <= 23.6 && lng >= 77.8 && lng <= 77.9) {
            return "Vidisha";
        }
        return "Unknown";
    }
}
