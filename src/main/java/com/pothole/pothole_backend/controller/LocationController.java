package com.pothole.pothole_backend.controller;

import com.pothole.pothole_backend.dto.response.ApiResponse;
import com.pothole.pothole_backend.model.City;
import com.pothole.pothole_backend.model.Ward;
import com.pothole.pothole_backend.model.Zone;
import com.pothole.pothole_backend.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping("/cities")
    public ResponseEntity<ApiResponse<List<City>>> getAllCities() {
        return ResponseEntity.ok(
                ApiResponse.success("Cities fetched", locationService.getAllCities()));
    }

    @GetMapping("/cities/{id}")
    public ResponseEntity<ApiResponse<City>> getCityById(@PathVariable Integer id) {
        return ResponseEntity.ok(
                ApiResponse.success("City fetched", locationService.getCityById(id)));
    }

    @GetMapping("/zones/{cityId}")
    public ResponseEntity<ApiResponse<List<Zone>>> getZonesByCity(
            @PathVariable Integer cityId) {
        return ResponseEntity.ok(
                ApiResponse.success("Zones fetched", locationService.getZonesByCity(cityId)));
    }

    @GetMapping("/wards/zone/{zoneId}")
    public ResponseEntity<ApiResponse<List<Ward>>> getWardsByZone(
            @PathVariable Integer zoneId) {
        return ResponseEntity.ok(
                ApiResponse.success("Wards fetched", locationService.getWardsByZone(zoneId)));
    }

    @GetMapping("/wards/city/{cityId}")
    public ResponseEntity<ApiResponse<List<Ward>>> getWardsByCity(
            @PathVariable Integer cityId) {
        return ResponseEntity.ok(
                ApiResponse.success("Wards fetched", locationService.getWardsByCity(cityId)));
    }
}
