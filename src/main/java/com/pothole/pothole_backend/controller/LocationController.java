package com.pothole.pothole_backend.controller;

import com.pothole.pothole_backend.dto.response.ApiResponse;
import com.pothole.pothole_backend.model.City;
import com.pothole.pothole_backend.model.Ward;
import com.pothole.pothole_backend.model.Zone;
import com.pothole.pothole_backend.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping("/cities")
    public ResponseEntity<ApiResponse<List<City>>> cities() {
        return ResponseEntity.ok(
                ApiResponse.success("Cities fetched",
                        locationService.getAllCities()));
    }

    @GetMapping("/cities/{id}")
    public ResponseEntity<ApiResponse<City>> cityById(
            @PathVariable Integer id) {
        return ResponseEntity.ok(
                ApiResponse.success("City fetched",
                        locationService.getCityById(id)));
    }

    // Frontend expects "id" field — map Zone to simple DTO
    @GetMapping("/zones/{cityId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> zones(
            @PathVariable Integer cityId) {
        List<Zone> zones = locationService.getZonesByCity(cityId);
        List<Map<String, Object>> result = zones.stream().map(z -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", z.getId());
            map.put("zoneName", z.getZoneName());
            map.put("zoneNumber", z.getZoneNumber());
            map.put("phone", z.getPhone());
            map.put("cityId", z.getCity() != null ? z.getCity().getId() : null);
            map.put("officeAddress", z.getOfficeAddress());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Zones fetched", result));
    }

    @GetMapping("/wards/zone/{zoneId}")
    public ResponseEntity<ApiResponse<List<Ward>>> wardsByZone(
            @PathVariable Integer zoneId) {
        return ResponseEntity.ok(
                ApiResponse.success("Wards fetched",
                        locationService.getWardsByZone(zoneId)));
    }

    @GetMapping("/wards/city/{cityId}")
    public ResponseEntity<ApiResponse<List<Ward>>> wardsByCity(
            @PathVariable Integer cityId) {
        return ResponseEntity.ok(
                ApiResponse.success("Wards fetched",
                        locationService.getWardsByCity(cityId)));
    }
}