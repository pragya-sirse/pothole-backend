package com.pothole.pothole_backend.controller;

import com.pothole.pothole_backend.dto.response.ApiResponse;
import com.pothole.pothole_backend.dto.response.ZoneSummaryResponse;
import com.pothole.pothole_backend.model.Pothole;
import com.pothole.pothole_backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary/{cityId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCitySummary(
            @PathVariable Integer cityId) {
        return ResponseEntity.ok(
                ApiResponse.success("Summary fetched",
                        dashboardService.getCitySummary(cityId)));
    }

    @GetMapping("/zones/{cityId}")
    public ResponseEntity<ApiResponse<List<ZoneSummaryResponse>>> getZoneSummary(
            @PathVariable Integer cityId) {
        return ResponseEntity.ok(
                ApiResponse.success("Zone summary fetched",
                        dashboardService.getZoneSummary(cityId)));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<Pothole>>> getPending() {
        return ResponseEntity.ok(
                ApiResponse.success("Pending potholes",
                        dashboardService.getPendingPotholes()));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Pothole>>> getAll() {
        return ResponseEntity.ok(
                ApiResponse.success("All potholes",
                        dashboardService.getAllPotholes()));
    }
}