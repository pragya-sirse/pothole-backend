package com.pothole.pothole_backend.service;


import com.pothole.pothole_backend.dto.response.ZoneSummaryResponse;
import com.pothole.pothole_backend.model.Pothole;
import com.pothole.pothole_backend.model.Zone;
import com.pothole.pothole_backend.repository.CityRepository;
import com.pothole.pothole_backend.repository.PotholeRepository;
import com.pothole.pothole_backend.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PotholeRepository potholeRepository;
    private final ZoneRepository zoneRepository;
    private final CityRepository cityRepository;

    public Map<String, Object> getCitySummary(Integer cityId) {
        List<Pothole> all = potholeRepository.findByCityId(cityId);

        long total    = all.size();
        long high     = all.stream().filter(p -> p.getSeverity() == Pothole.Severity.high).count();
        long medium   = all.stream().filter(p -> p.getSeverity() == Pothole.Severity.medium).count();
        long low      = all.stream().filter(p -> p.getSeverity() == Pothole.Severity.low).count();
        long pending  = all.stream().filter(p -> p.getStatus() == Pothole.Status.pending).count();
        long progress = all.stream().filter(p -> p.getStatus() == Pothole.Status.in_progress).count();
        long done     = all.stream().filter(p -> p.getStatus() == Pothole.Status.completed).count();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalPotholes", total);
        summary.put("highSeverity", high);
        summary.put("mediumSeverity", medium);
        summary.put("lowSeverity", low);
        summary.put("pending", pending);
        summary.put("inProgress", progress);
        summary.put("completed", done);

        return summary;
    }

    public List<ZoneSummaryResponse> getZoneSummary(Integer cityId) {
        List<Zone> zones = zoneRepository.findByCityId(cityId);
        List<ZoneSummaryResponse> result = new ArrayList<>();

        for (Zone zone : zones) {
            List<Pothole> zonePotholes = potholeRepository.findByZoneId(zone.getId());

            ZoneSummaryResponse zsr = ZoneSummaryResponse.builder()
                    .city(zone.getCity().getName())
                    .zoneName(zone.getZoneName())
                    .contact(zone.getPhone())
                    .totalPotholes((long) zonePotholes.size())
                    .highSeverity(zonePotholes.stream()
                            .filter(p -> p.getSeverity() == Pothole.Severity.high).count())
                    .mediumSeverity(zonePotholes.stream()
                            .filter(p -> p.getSeverity() == Pothole.Severity.medium).count())
                    .lowSeverity(zonePotholes.stream()
                            .filter(p -> p.getSeverity() == Pothole.Severity.low).count())
                    .pending(zonePotholes.stream()
                            .filter(p -> p.getStatus() == Pothole.Status.pending).count())
                    .inProgress(zonePotholes.stream()
                            .filter(p -> p.getStatus() == Pothole.Status.in_progress).count())
                    .completed(zonePotholes.stream()
                            .filter(p -> p.getStatus() == Pothole.Status.completed).count())
                    .build();

            result.add(zsr);
        }
        return result;
    }

    public List<Pothole> getPendingPotholes() {
        return potholeRepository.findByStatus(Pothole.Status.pending);
    }

    public List<Pothole> getAllPotholes() {
        return potholeRepository.findAll();
    }
}
