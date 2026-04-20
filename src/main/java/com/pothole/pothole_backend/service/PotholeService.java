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


    private final PotholeRepository potholeRepository;
    private final CityRepository cityRepository;
    private final ZoneRepository zoneRepository;
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final UpvoteRepository upvoteRepository;
    private final NotificationRepository notificationRepository;
    private final MlService mlService;
    private final EmailService emailService;

    // ── REPORT NEW POTHOLE ────────────────────────────────
    public PotholeResponse reportPothole(PotholeReportRequest req,
                                         MultipartFile image,
                                         String userEmail) {
        // 1. Get user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Get city
        City city = cityRepository.findById(req.getCityId())
                .orElseThrow(() -> new RuntimeException("City not found"));

        // 3. Duplicate check within 500m
        List<Pothole> nearby = potholeRepository
                .findNearby(req.getLatitude(), req.getLongitude());
        if (!nearby.isEmpty()) {
            Pothole existing = nearby.get(0);
            upvotePothole(existing.getId(), userEmail);
            return mapToResponse(existing);
        }

        // 4. ML detection
        Map<String, Object> ml = mlService.detectPothole(image);
        String severity  = (String) ml.getOrDefault("severity", "medium");
        Float confidence = ((Number) ml.getOrDefault("confidence", 0.75)).floatValue();
        Float sevScore   = ((Number) ml.getOrDefault("severity_score", 0.60)).floatValue();
        Integer bboxW    = ((Number) ml.getOrDefault("bbox_width", 100)).intValue();
        Integer bboxH    = ((Number) ml.getOrDefault("bbox_height", 100)).intValue();

        // 5. Get first zone of city
        List<Zone> zones = zoneRepository.findByCityId(city.getId());
        if (zones.isEmpty()) throw new RuntimeException("No zones configured for city");
        Zone zone = zones.get(0);

        // 6. Road type
        String roadType = (req.getRoadType() != null
                && !req.getRoadType().isEmpty())
                ? req.getRoadType() : "unknown";

        // 7. Priority score
        int priority = calcPriority(severity, 0, roadType);

        // 8. Image URL placeholder
        String imageUrl = (image != null && !image.isEmpty())
                ? "https://res.cloudinary.com/demo/ph_" + System.currentTimeMillis() + ".jpg"
                : "https://res.cloudinary.com/demo/placeholder.jpg";

        // 9. Build and save pothole
        Pothole pothole = Pothole.builder()
                .city(city)
                .zone(zone)
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .imageUrl(imageUrl)
                .severity(Pothole.Severity.valueOf(severity))
                .severityScore(sevScore)
                .confidenceScore(confidence)
                .bboxWidth(bboxW)
                .bboxHeight(bboxH)
                .detectedBy(Pothole.DetectedBy.citizen)
                .status(Pothole.Status.pending)
                .priorityScore(priority)
                .upvoteCount(0)
                .roadType(Pothole.RoadType.valueOf(roadType))
                .reportedBy(user)
                .build();

        potholeRepository.save(pothole);
        log.info("Pothole saved: id={}, user={}, city={}",
                pothole.getId(), userEmail, city.getName());

        // 10. Auto assign to zone authority
        authorityRepository.findFirstByZoneIdAndIsActiveTrue(zone.getId())
                .ifPresent(auth -> {
                    pothole.setAssignedTo(auth);
                    potholeRepository.save(pothole);

                    // Save notification
                    notificationRepository.save(Notification.builder()
                            .pothole(pothole)
                            .authority(auth)
                            .type(Notification.Type.new_report)
                            .sentTo(auth.getEmail())
                            .isSent(false)
                            .build());

                    // Send email
                    if (auth.getEmail() != null && !auth.getEmail().isBlank()) {
                        emailService.sendNewPotholeAlert(
                                auth.getEmail(),
                                zone.getZoneName(),
                                severity,
                                req.getLatitude(),
                                req.getLongitude());
                    }
                });

        return mapToResponse(pothole);
    }

    // ── MAP DATA ──────────────────────────────────────────
    public List<MapPotholeResponse> getForMap(Integer cityId) {
        return potholeRepository.findByCityIdOrderByPriority(cityId)
                .stream().map(p -> MapPotholeResponse.builder()
                        .id(p.getId())
                        .latitude(p.getLatitude())
                        .longitude(p.getLongitude())
                        .severity(p.getSeverity().name())
                        .status(p.getStatus().name())
                        .upvoteCount(p.getUpvoteCount())
                        .imageUrl(p.getImageUrl())
                        .zoneName(p.getZone().getZoneName())
                        .build())
                .collect(Collectors.toList());
    }

    // ── GET BY ID ─────────────────────────────────────────
    public PotholeResponse getById(Integer id) {
        return mapToResponse(potholeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pothole not found")));
    }

    // ── GET BY CITY ───────────────────────────────────────
    public List<PotholeResponse> getByCity(Integer cityId) {
        return potholeRepository.findByCityId(cityId)
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── GET BY ZONE ───────────────────────────────────────
    public List<PotholeResponse> getByZone(Integer zoneId) {
        return potholeRepository.findByZoneId(zoneId)
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── UPVOTE ────────────────────────────────────────────
    public Map<String, Object> upvotePothole(Integer id, String email) {
        Pothole p = potholeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pothole not found"));
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (upvoteRepository.existsByPotholeIdAndUserId(id, u.getId()))
            return Map.of("message", "Already upvoted",
                    "count", p.getUpvoteCount());

        upvoteRepository.save(Upvote.builder()
                .pothole(p).user(u).build());

        p.setUpvoteCount(p.getUpvoteCount() + 1);
        p.setPriorityScore(calcPriority(
                p.getSeverity().name(),
                p.getUpvoteCount(),
                p.getRoadType().name()));
        potholeRepository.save(p);

        return Map.of("message", "Upvoted successfully!",
                "count", p.getUpvoteCount());
    }

    // ── UPDATE STATUS ─────────────────────────────────────
    public PotholeResponse updateStatus(Integer id, StatusUpdateRequest req) {
        Pothole p = potholeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pothole not found"));

        p.setStatus(Pothole.Status.valueOf(req.getStatus()));
        if (req.getNotes() != null) p.setNotes(req.getNotes());
        potholeRepository.save(p);

        if (p.getReportedBy() != null
                && p.getReportedBy().getEmail() != null) {
            emailService.sendStatusUpdate(
                    p.getReportedBy().getEmail(),
                    p.getReportedBy().getName(),
                    id, req.getStatus());
        }

        return mapToResponse(p);
    }

    // ── MY REPORTS ────────────────────────────────────────
    public List<PotholeResponse> getMyReports(String email) {
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return potholeRepository.findByReportedById(u.getId())
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── HELPER: Priority Score ────────────────────────────
    private int calcPriority(String sev, int votes, String road) {
        int s = votes * 2;
        s += switch (sev.toLowerCase()) {
            case "high"   -> 40;
            case "medium" -> 20;
            default       -> 5;
        };
        s += switch (road.toLowerCase()) {
            case "highway"   -> 30;
            case "main_road" -> 20;
            case "internal"  -> 10;
            default          -> 0;
        };
        return s;
    }

    // ── HELPER: Map to Response ───────────────────────────
    private PotholeResponse mapToResponse(Pothole p) {
        return PotholeResponse.builder()
                .id(p.getId())
                .city(p.getCity() != null ? p.getCity().getName() : null)
                .zoneName(p.getZone() != null ? p.getZone().getZoneName() : null)
                .zonePhone(p.getZone() != null ? p.getZone().getPhone() : null)
                .wardNumber(p.getWard() != null ? p.getWard().getWardNumber() : null)
                .wardName(p.getWard() != null ? p.getWard().getWardName() : null)
                .latitude(p.getLatitude())
                .longitude(p.getLongitude())
                .imageUrl(p.getImageUrl())
                .severity(p.getSeverity() != null ? p.getSeverity().name() : null)
                .severityScore(p.getSeverityScore())
                .confidenceScore(p.getConfidenceScore())
                .status(p.getStatus() != null ? p.getStatus().name() : null)
                .upvoteCount(p.getUpvoteCount())
                .priorityScore(p.getPriorityScore())
                .roadType(p.getRoadType() != null ? p.getRoadType().name() : null)
                .detectedBy(p.getDetectedBy() != null ? p.getDetectedBy().name() : null)
                .reportedByName(p.getReportedBy() != null
                        ? p.getReportedBy().getName() : "Auto")
                .assignedOfficer(p.getAssignedTo() != null
                        ? p.getAssignedTo().getName() : null)
                .createdAt(p.getCreatedAt())
                .build();

    }
}
