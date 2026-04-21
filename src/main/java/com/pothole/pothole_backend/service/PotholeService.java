package com.pothole.pothole_backend.service;

import com.pothole.pothole_backend.dto.request.PotholeReportRequest;
import com.pothole.pothole_backend.dto.request.StatusUpdateRequest;
import com.pothole.pothole_backend.dto.response.MapPotholeResponse;
import com.pothole.pothole_backend.dto.response.PotholeResponse;
import com.pothole.pothole_backend.model.*;
import com.pothole.pothole_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
@Slf4j
public class PotholeService {

    private final PotholeRepository      potholeRepository;
    private final CityRepository         cityRepository;
    private final ZoneRepository         zoneRepository;
    private final UserRepository         userRepository;
    private final AuthorityRepository    authorityRepository;
    private final UpvoteRepository       upvoteRepository;
    private final NotificationRepository notificationRepository;
    private final MlService              mlService;
    private final EmailService           emailService;

    // ── REPORT NEW POTHOLE ─────────────────────────────────
    public PotholeResponse reportPothole(PotholeReportRequest req,
                                         MultipartFile image,
                                         String userEmail) {
        // 1. Get user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Get city
        City city = cityRepository.findById(req.getCityId())
                .orElseThrow(() -> new RuntimeException("City not found"));

        // 3. Duplicate check within ~500m
        try {
            List<Pothole> nearby = potholeRepository
                    .findNearby(req.getLatitude(), req.getLongitude());
            if (!nearby.isEmpty()) {
                Pothole existing = nearby.get(0);
                log.info("Duplicate found id={}, upvoting", existing.getId());
                upvotePothole(existing.getId(), userEmail);
                return mapToResponse(existing);
            }
        } catch (Exception e) {
            log.warn("Nearby check failed: {}", e.getMessage());
        }

        // 4. ML detection
        String  severity   = "medium";
        Float   confidence = 0.75f;
        Float   sevScore   = 0.60f;
        Integer bboxW      = 100;
        Integer bboxH      = 100;
        try {
            Map<String, Object> ml = mlService.detectPothole(image);
            severity   = String.valueOf(ml.getOrDefault("severity",      "medium"));
            confidence = ((Number) ml.getOrDefault("confidence",         0.75)).floatValue();
            sevScore   = ((Number) ml.getOrDefault("severity_score",     0.60)).floatValue();
            bboxW      = ((Number) ml.getOrDefault("bbox_width",         100)).intValue();
            bboxH      = ((Number) ml.getOrDefault("bbox_height",        100)).intValue();
        } catch (Exception e) {
            log.warn("ML failed, using fallback: {}", e.getMessage());
        }

        // 5. Get first zone of city
        List<Zone> zones = zoneRepository.findByCityId(city.getId());
        if (zones.isEmpty())
            throw new RuntimeException("No zones for city: " + city.getName());
        Zone zone = zones.get(0);

        // 6. Parse enums safely
        Pothole.Severity severityEnum = parseSeverity(severity);
        Pothole.RoadType roadTypeEnum = parseRoadType(req.getRoadType());

        // 7. Priority
        int priority = calcPriority(severityEnum.name(), 0, roadTypeEnum.name());

        // 8. Image URL
        String imageUrl = "https://res.cloudinary.com/demo/ph_"
                + System.currentTimeMillis() + ".jpg";

        // 9. Save pothole
        Pothole pothole = Pothole.builder()
                .city(city).zone(zone)
                .latitude(req.getLatitude()).longitude(req.getLongitude())
                .imageUrl(imageUrl)
                .severity(severityEnum)
                .severityScore(sevScore)
                .confidenceScore(confidence)
                .bboxWidth(bboxW).bboxHeight(bboxH)
                .detectedBy(Pothole.DetectedBy.citizen)
                .status(Pothole.Status.pending)
                .priorityScore(priority).upvoteCount(0)
                .roadType(roadTypeEnum)
                .reportedBy(user)
                .notes(req.getDescription())
                .build();

        potholeRepository.save(pothole);
        log.info("Pothole saved: id={}, city={}, severity={}",
                pothole.getId(), city.getName(), severityEnum);

        // 10. Auto-assign + notify authority with EMAIL
        try {
            authorityRepository.findFirstByZoneIdAndIsActiveTrue(zone.getId())
                    .ifPresent(auth -> {
                        pothole.setAssignedTo(auth);
                        potholeRepository.save(pothole);

                        notificationRepository.save(Notification.builder()
                                .pothole(pothole).authority(auth)
                                .type(Notification.Type.new_report)
                                .sentTo(auth.getEmail()).isSent(false).build());

                        // Send email to zone authority
                        if (auth.getEmail() != null && !auth.getEmail().isBlank()) {
                            emailService.sendNewPotholeAlert(
                                    auth.getEmail(),
                                    zone.getZoneName(),
                                    severityEnum.name(),
                                    req.getLatitude(),
                                    req.getLongitude(),
                                    pothole.getId(),
                                    user.getName()
                            );
                        } else {
                            log.warn("Authority {} has no email, skipping notification",
                                    auth.getName());
                        }
                    });
        } catch (Exception e) {
            log.warn("Authority assign failed: {}", e.getMessage());
        }

        return mapToResponse(pothole);
    }

    // ── MAP DATA ───────────────────────────────────────────
    public List<MapPotholeResponse> getForMap(Integer cityId) {
        return potholeRepository.findByCityIdOrderByPriority(cityId)
                .stream().map(p -> MapPotholeResponse.builder()
                        .id(p.getId())
                        .latitude(p.getLatitude())
                        .longitude(p.getLongitude())
                        .severity(p.getSeverity() != null ? p.getSeverity().name() : "medium")
                        .status(p.getStatus() != null ? p.getStatus().name() : "pending")
                        .upvoteCount(p.getUpvoteCount() != null ? p.getUpvoteCount() : 0)
                        .imageUrl(p.getImageUrl())
                        .zoneName(p.getZone() != null ? p.getZone().getZoneName() : "")
                        .build())
                .collect(Collectors.toList());
    }

    // ── GET BY ID ──────────────────────────────────────────
    public PotholeResponse getById(Integer id) {
        return mapToResponse(potholeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pothole not found")));
    }

    // ── GET BY CITY ────────────────────────────────────────
    public List<PotholeResponse> getByCity(Integer cityId) {
        return potholeRepository.findByCityId(cityId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ── GET BY ZONE ────────────────────────────────────────
    public List<PotholeResponse> getByZone(Integer zoneId) {
        return potholeRepository.findByZoneId(zoneId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ── UPVOTE ─────────────────────────────────────────────
    public Map<String, Object> upvotePothole(Integer id, String email) {
        Pothole p = potholeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pothole not found"));
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (upvoteRepository.existsByPotholeIdAndUserId(id, u.getId()))
            return Map.of("message", "Already upvoted",
                    "count", p.getUpvoteCount() != null ? p.getUpvoteCount() : 0);

        upvoteRepository.save(Upvote.builder().pothole(p).user(u).build());
        int newCount = (p.getUpvoteCount() != null ? p.getUpvoteCount() : 0) + 1;
        p.setUpvoteCount(newCount);
        p.setPriorityScore(calcPriority(
                p.getSeverity() != null ? p.getSeverity().name() : "medium",
                newCount,
                p.getRoadType() != null ? p.getRoadType().name() : "unknown"));
        potholeRepository.save(p);
        return Map.of("message", "Upvoted successfully!", "count", newCount);
    }

    // ── UPDATE STATUS ──────────────────────────────────────
    public PotholeResponse updateStatus(Integer id, StatusUpdateRequest req) {
        Pothole p = potholeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pothole not found"));

        try {
            p.setStatus(Pothole.Status.valueOf(req.getStatus().toLowerCase()));
        } catch (Exception e) {
            throw new RuntimeException("Invalid status. Use: pending, in_progress, completed, rejected");
        }

        if (req.getNotes() != null && !req.getNotes().isBlank())
            p.setNotes(req.getNotes());
        potholeRepository.save(p);

        // Notify citizen
        try {
            if (p.getReportedBy() != null && p.getReportedBy().getEmail() != null)
                emailService.sendStatusUpdate(p.getReportedBy().getEmail(),
                        p.getReportedBy().getName(), id, req.getStatus());
        } catch (Exception e) {
            log.warn("Status email failed: {}", e.getMessage());
        }

        return mapToResponse(p);
    }

    // ── MY REPORTS ─────────────────────────────────────────
    public List<PotholeResponse> getMyReports(String email) {
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return potholeRepository.findByReportedById(u.getId())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ── PRIVATE HELPERS ────────────────────────────────────
    private Pothole.Severity parseSeverity(String s) {
        if (s == null || s.isBlank()) return Pothole.Severity.medium;
        try { return Pothole.Severity.valueOf(s.toLowerCase().trim()); }
        catch (Exception e) { return Pothole.Severity.medium; }
    }

    private Pothole.RoadType parseRoadType(String r) {
        if (r == null || r.isBlank()) return Pothole.RoadType.unknown;
        try { return Pothole.RoadType.valueOf(r.toLowerCase().trim()); }
        catch (Exception e) { return Pothole.RoadType.unknown; }
    }

    private int calcPriority(String sev, int votes, String road) {
        int s = votes * 2;
        s += switch (sev.toLowerCase()) { case "high" -> 40; case "medium" -> 20; default -> 5; };
        s += switch (road.toLowerCase()) { case "highway" -> 30; case "main_road" -> 20; case "internal" -> 10; default -> 0; };
        return s;
    }

    private PotholeResponse mapToResponse(Pothole p) {
        if (p == null) return null;
        try {
            return PotholeResponse.builder()
                    .id(p.getId())
                    .city(p.getCity() != null ? p.getCity().getName() : null)
                    .zoneName(p.getZone() != null ? p.getZone().getZoneName() : null)
                    .zonePhone(p.getZone() != null ? p.getZone().getPhone() : null)
                    .wardNumber(p.getWard() != null ? p.getWard().getWardNumber() : null)
                    .wardName(p.getWard() != null ? p.getWard().getWardName() : null)
                    .latitude(p.getLatitude()).longitude(p.getLongitude())
                    .imageUrl(p.getImageUrl())
                    .severity(p.getSeverity() != null ? p.getSeverity().name() : null)
                    .severityScore(p.getSeverityScore())
                    .confidenceScore(p.getConfidenceScore())
                    .status(p.getStatus() != null ? p.getStatus().name() : null)
                    .upvoteCount(p.getUpvoteCount() != null ? p.getUpvoteCount() : 0)
                    .priorityScore(p.getPriorityScore() != null ? p.getPriorityScore() : 0)
                    .roadType(p.getRoadType() != null ? p.getRoadType().name() : null)
                    .detectedBy(p.getDetectedBy() != null ? p.getDetectedBy().name() : null)
                    .reportedByName(p.getReportedBy() != null ? p.getReportedBy().getName() : "Auto")
                    .assignedOfficer(p.getAssignedTo() != null ? p.getAssignedTo().getName() : null)
                    .createdAt(p.getCreatedAt())
                    .build();
        } catch (Exception e) {
            log.error("mapToResponse failed: {}", e.getMessage());
            throw new RuntimeException("Mapping failed: " + e.getMessage());
        }
    }
}
