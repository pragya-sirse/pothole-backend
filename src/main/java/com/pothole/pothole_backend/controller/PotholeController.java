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
    // ── QUICK STATUS UPDATE (Email link se) ──────────────────
    @GetMapping("/{id}/quick-status")
    public ResponseEntity<String> quickStatusUpdate(
            @PathVariable Integer id,
            @RequestParam String status) {
        try {
            StatusUpdateRequest req = new StatusUpdateRequest();
            req.setStatus(status);
            req.setNotes("Updated via email link by zone authority");
            potholeService.updateStatus(id, req);

            String color = status.equals("completed") ? "#28a745" :
                    status.equals("in_progress") ? "#007bff" : "#ffc107";
            String emoji = status.equals("completed") ? "✅" :
                    status.equals("in_progress") ? "🔧" : "📋";
            String label = status.equals("completed") ? "Completed" :
                    status.equals("in_progress") ? "In Progress" : status;

            String html = """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width,initial-scale=1">
              <title>Status Updated</title>
            </head>
            <body style="font-family:Arial,sans-serif;background:#f5f5f5;
                  margin:0;padding:40px 20px;text-align:center;">
              <div style="max-width:500px;margin:0 auto;background:white;
                   border-radius:12px;padding:40px;
                   box-shadow:0 4px 20px rgba(0,0,0,0.1);">
                <div style="font-size:64px;margin-bottom:16px;">%s</div>
                <h2 style="color:#1F3864;margin-bottom:8px;">
                  Status Updated Successfully
                </h2>
                <div style="background:%s;color:white;padding:12px 24px;
                     border-radius:6px;display:inline-block;
                     font-size:18px;font-weight:bold;margin:16px 0;">
                  %s
                </div>
                <p style="color:#555;margin-top:16px;">
                  Pothole Report <strong>#%d</strong> has been marked
                  as <strong>%s</strong>.
                </p>
                <p style="color:#555;">
                  The citizen will be notified automatically.
                  The dashboard has been updated.
                </p>
                <hr style="border:none;border-top:1px solid #eee;margin:24px 0;">
                <p style="color:#888;font-size:13px;">
                  Smart Pothole Detection System<br>
                  SATI Vidisha, Madhya Pradesh
                </p>
              </div>
            </body>
            </html>
            """.formatted(emoji, color, label, id, label);

            return ResponseEntity.ok()
                    .header("Content-Type", "text/html; charset=UTF-8")
                    .body(html);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .header("Content-Type", "text/html; charset=UTF-8")
                    .body("<h2>Error: " + e.getMessage() + "</h2>");
        }

    }

}