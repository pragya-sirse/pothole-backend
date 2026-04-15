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

    // PUBLIC - Map view (no login needed)
    @GetMapping("/map/{cityId}")
    public ResponseEntity<ApiResponse<List<MapPotholeResponse>>> getMapPotholes(
            @PathVariable Integer cityId) {
        return ResponseEntity.ok(
                ApiResponse.success("Map data fetched",
                        potholeService.getPotholesForMap(cityId)));
    }

    // PUBLIC - Single pothole detail
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PotholeResponse>> getPotholeById(
            @PathVariable Integer id) {
        return ResponseEntity.ok(
                ApiResponse.success("Pothole fetched",
                        potholeService.getPotholeById(id)));
    }

    // PUBLIC - All potholes by city
    @GetMapping("/city/{cityId}")
    public ResponseEntity<ApiResponse<List<PotholeResponse>>> getPotholesByCity(
            @PathVariable Integer cityId) {
        return ResponseEntity.ok(
                ApiResponse.success("Potholes fetched",
                        potholeService.getPotholesByCity(cityId)));
    }

    // PUBLIC - By zone
    @GetMapping("/zone/{zoneId}")
    public ResponseEntity<ApiResponse<List<PotholeResponse>>> getPotholesByZone(
            @PathVariable Integer zoneId) {
        return ResponseEntity.ok(
                ApiResponse.success("Potholes fetched",
                        potholeService.getPotholesByZone(zoneId)));
    }

    // PROTECTED - Report new pothole (needs login)
    @PostMapping(value = "/report",
            consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<PotholeResponse>> report(
            @RequestPart("data") String dataJson,
            @RequestPart(value = "image", required = false) MultipartFile image,
            Authentication auth) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PotholeReportRequest req = mapper.readValue(dataJson,
                PotholeReportRequest.class);
        PotholeResponse response = potholeService.reportPothole(req, image, auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Pothole reported successfully", response));
    }

    // PROTECTED - Upvote
    @PutMapping("/{id}/upvote")
    public ResponseEntity<ApiResponse<Map<String, Object>>> upvote(
            @PathVariable Integer id,
            Authentication auth) {
        Map<String, Object> result = potholeService.upvotePothole(id, auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Upvote recorded", result));
    }

    // PROTECTED - My reports
    @GetMapping("/my-reports")
    public ResponseEntity<ApiResponse<List<PotholeResponse>>> myReports(
            Authentication auth) {
        return ResponseEntity.ok(
                ApiResponse.success("Your reports",
                        potholeService.getMyReports(auth.getName())));
    }

    // PROTECTED - Update status (authority)
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<PotholeResponse>> updateStatus(
            @PathVariable Integer id,
            @Valid @RequestBody StatusUpdateRequest req) {
        return ResponseEntity.ok(
                ApiResponse.success("Status updated",
                        potholeService.updateStatus(id, req)));
    }
}