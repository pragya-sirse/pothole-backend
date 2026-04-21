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
    public ResponseEntity<ApiResponse<Map<String, Object>>> summary(@PathVariable Integer cityId) {
        return ResponseEntity.ok(ApiResponse.success("Summary", dashboardService.getCitySummary(cityId)));
    }

    @GetMapping("/zones/{cityId}")
    public ResponseEntity<ApiResponse<List<ZoneSummaryResponse>>> zones(@PathVariable Integer cityId) {
        return ResponseEntity.ok(ApiResponse.success("Zone summary", dashboardService.getZoneSummary(cityId)));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<Pothole>>> pending() {
        return ResponseEntity.ok(ApiResponse.success("Pending", dashboardService.getPendingPotholes()));
    }
}