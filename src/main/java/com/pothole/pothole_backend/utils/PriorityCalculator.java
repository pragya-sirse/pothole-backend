package com.pothole.pothole_backend.utils;

import org.springframework.stereotype.Component;

@Component
public class PriorityCalculator {

    public int calculate(String severity, int upvotes, String roadType) {
        int score = upvotes * 2;

        score += switch (severity.toLowerCase()) {
            case "high"   -> 40;
            case "medium" -> 20;
            default       -> 5;
        };

        score += switch (roadType.toLowerCase()) {
            case "highway"   -> 30;
            case "main_road" -> 20;
            case "internal"  -> 10;
            default          -> 0;
        };

        return score;
    }
}