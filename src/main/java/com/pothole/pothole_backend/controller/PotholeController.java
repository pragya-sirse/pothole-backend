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
    @GetMapping("/{id}/quick-update")
    public ResponseEntity<String> quickUpdate(
            @PathVariable Integer id,
            @RequestParam String status,
            @RequestParam(required = false) String token) {

        try {
            StatusUpdateRequest req = new StatusUpdateRequest();
            req.setStatus(status);
            req.setNotes("Updated by zone authority via email link");
            potholeService.updateStatus(id, req);

            String color = switch (status.toLowerCase()) {
                case "completed"   -> "#10b981";
                case "in_progress" -> "#3b82f6";
                default            -> "#f59e0b";
            };
            String icon = switch (status.toLowerCase()) {
                case "completed"   -> "✅";
                case "in_progress" -> "🔧";
                default            -> "⏳";
            };

            String html = "<!DOCTYPE html><html><body style='font-family:Arial,sans-serif;"
                    + "background:#f0fdf4;display:flex;align-items:center;justify-content:center;"
                    + "min-height:100vh;margin:0'>"
                    + "<div style='background:white;border-radius:16px;padding:48px;text-align:center;"
                    + "box-shadow:0 4px 24px rgba(0,0,0,0.1);max-width:400px'>"
                    + "<div style='font-size:64px;margin-bottom:16px'>" + icon + "</div>"
                    + "<h2 style='color:" + color + ";margin:0 0 12px'>"
                    + "Status Updated!</h2>"
                    + "<p style='color:#666;margin:0 0 8px'>Pothole #" + id + "</p>"
                    + "<div style='background:" + color + "22;border:2px solid " + color
                    + ";border-radius:8px;padding:12px;margin:16px 0'>"
                    + "<strong style='color:" + color + ";font-size:18px'>"
                    + status.replace("_", " ").toUpperCase() + "</strong>"
                    + "</div>"
                    + "<p style='color:#888;font-size:13px'>"
                    + "The citizen has been notified automatically.</p>"
                    + "<p style='color:#aaa;font-size:11px;margin-top:24px'>"
                    + "RoadWatch MP — Smart Pothole Detection System</p>"
                    + "</div></body></html>";

            return ResponseEntity.ok()
                    .header("Content-Type", "text/html; charset=UTF-8")
                    .body(html);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .header("Content-Type", "text/html; charset=UTF-8")
                    .body("<html><body style='font-family:Arial;text-align:center;"
                            + "padding:50px'><h2>Error: " + e.getMessage()
                            + "</h2></body></html>");
        }
    }

}