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

    public Map<String, Object> getCitySummary(Integer cityId) {
        List<Pothole> all = potholeRepository.findByCityId(cityId);
        Map<String, Object> m = new HashMap<>();
        m.put("total",      all.size());
        m.put("high",       all.stream().filter(p -> p.getSeverity() == Pothole.Severity.high).count());
        m.put("medium",     all.stream().filter(p -> p.getSeverity() == Pothole.Severity.medium).count());
        m.put("low",        all.stream().filter(p -> p.getSeverity() == Pothole.Severity.low).count());
        m.put("pending",    all.stream().filter(p -> p.getStatus() == Pothole.Status.pending).count());
        m.put("inProgress", all.stream().filter(p -> p.getStatus() == Pothole.Status.in_progress).count());
        m.put("completed",  all.stream().filter(p -> p.getStatus() == Pothole.Status.completed).count());
        return m;
    }

    public List<ZoneSummaryResponse> getZoneSummary(Integer cityId) {
        List<Zone> zones = zoneRepository.findByCityId(cityId);
        List<ZoneSummaryResponse> result = new ArrayList<>();
        for (Zone z : zones) {
            List<Pothole> zp = potholeRepository.findByZoneId(z.getId());
            result.add(ZoneSummaryResponse.builder()
                    .city(z.getCity() != null ? z.getCity().getName() : "")
                    .zoneName(z.getZoneName())
                    .contact(z.getPhone())
                    .totalPotholes((long) zp.size())
                    .highSeverity(zp.stream().filter(p -> p.getSeverity() == Pothole.Severity.high).count())
                    .mediumSeverity(zp.stream().filter(p -> p.getSeverity() == Pothole.Severity.medium).count())
                    .lowSeverity(zp.stream().filter(p -> p.getSeverity() == Pothole.Severity.low).count())
                    .pending(zp.stream().filter(p -> p.getStatus() == Pothole.Status.pending).count())
                    .inProgress(zp.stream().filter(p -> p.getStatus() == Pothole.Status.in_progress).count())
                    .completed(zp.stream().filter(p -> p.getStatus() == Pothole.Status.completed).count())
                    .build());
        }
        return result;
    }

    public List<Pothole> getPendingPotholes() {
        return potholeRepository.findByStatus(Pothole.Status.pending);
    }
}
