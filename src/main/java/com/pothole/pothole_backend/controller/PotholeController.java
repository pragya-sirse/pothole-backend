package com.pothole.pothole_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pothole.pothole_backend.dto.request.PotholeReportRequest;
import com.pothole.pothole_backend.dto.request.StatusUpdateRequest;
import com.pothole.pothole_backend.dto.response.ApiResponse;
import com.pothole.pothole_backend.dto.response.MapPotholeResponse;
import com.pothole.pothole_backend.dto.response.PotholeResponse;
import com.pothole.pothole_backend.service.PotholeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/potholes")
@RequiredArgsConstructor
public class PotholeController {

    private final PotholeService potholeService;
    private final ObjectMapper   objectMapper;

    @GetMapping("/map/{cityId}")
    public ResponseEntity<ApiResponse<List<MapPotholeResponse>>> map(@PathVariable Integer cityId) {
        return ResponseEntity.ok(ApiResponse.success("Map data", potholeService.getForMap(cityId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PotholeResponse>> getOne(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Pothole", potholeService.getById(id)));
    }

    @GetMapping("/city/{cityId}")
    public ResponseEntity<ApiResponse<List<PotholeResponse>>> byCity(@PathVariable Integer cityId) {
        return ResponseEntity.ok(ApiResponse.success("Potholes", potholeService.getByCity(cityId)));
    }

    @GetMapping("/zone/{zoneId}")
    public ResponseEntity<ApiResponse<List<PotholeResponse>>> byZone(@PathVariable Integer zoneId) {
        return ResponseEntity.ok(ApiResponse.success("Potholes", potholeService.getByZone(zoneId)));
    }

    @PostMapping(value = "/report", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<PotholeResponse>> report(
            @RequestPart(value = "data", required = false) String dataJson,
            @RequestPart(value = "image", required = false) MultipartFile image,
            Authentication auth) {
        try {
            if (dataJson == null || dataJson.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("data field is required"));
            }
            PotholeReportRequest req = objectMapper.readValue(
                    dataJson, PotholeReportRequest.class);
            return ResponseEntity.ok(ApiResponse.success(
                    "Pothole reported successfully",
                    potholeService.reportPothole(req, image, auth.getName())));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Report failed: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/upvote")
    public ResponseEntity<ApiResponse<Map<String, Object>>> upvote(
            @PathVariable Integer id, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Done",
                potholeService.upvotePothole(id, auth.getName())));
    }

    @GetMapping("/my-reports")
    public ResponseEntity<ApiResponse<List<PotholeResponse>>> mine(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Your reports",
                potholeService.getMyReports(auth.getName())));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<PotholeResponse>> updateStatus(
            @PathVariable Integer id,
            @Valid @RequestBody StatusUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Status updated",
                potholeService.updateStatus(id, req)));
    }
}