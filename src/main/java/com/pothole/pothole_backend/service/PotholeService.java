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
    private final WardRepository wardRepository;
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final UpvoteRepository upvoteRepository;
    private final NotificationRepository notificationRepository;
    private final MlService mlService;
    private final EmailService emailService;

    // ── REPORT A NEW POTHOLE ─────────────────────────────────
    public PotholeResponse reportPothole(PotholeReportRequest req,
                                         MultipartFile image,
                                         String userEmail) {

        // 1. Get user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Get city
        City city = cityRepository.findById(req.getCityId())
                .orElseThrow(() -> new RuntimeException("City not found"));

        // 3. Find nearby existing pothole (duplicate check within ~500m)
        List<Pothole> nearby = potholeRepository.findNearby(req.getLatitude(), req.getLongitude());
        if (!nearby.isEmpty()) {
            // Auto upvote existing one instead
            Pothole existing = nearby.get(0);
            upvotePothole(existing.getId(), userEmail);
            return mapToResponse(existing);
        }

        // 4. Call ML service for detection
        Map<String, Object> mlResult = mlService.detectPothole(image);
        boolean detected = (boolean) mlResult.getOrDefault("pothole_detected", true);
        String severity   = (String) mlResult.getOrDefault("severity", "medium");
        Double confidence = ((Number) mlResult.getOrDefault("confidence", 0.75)).doubleValue();
        Double sevScore   = ((Number) mlResult.getOrDefault("severity_score", 0.60)).doubleValue();
        Integer bboxW     = ((Number) mlResult.getOrDefault("bbox_width", 100)).intValue();
        Integer bboxH     = ((Number) mlResult.getOrDefault("bbox_height", 100)).intValue();

        // 5. Find zone by city (simple assignment — first zone of city)
        List<Zone> zones = zoneRepository.findByCityId(city.getId());
        if (zones.isEmpty()) throw new RuntimeException("No zones configured for this city");
        Zone zone = zones.get(0); // In production: use GPS polygon matching

        // 6. Image URL (Cloudinary will be integrated here)
        String imageUrl = "https://res.cloudinary.com/demo/pothole_" +
                System.currentTimeMillis() + ".jpg";

        // 7. Calculate priority score
        int priority = calculatePriority(severity, 0,
                req.getRoadType() != null ? req.getRoadType() : "unknown");

        // 8. Build and save pothole
        Pothole pothole = Pothole.builder()
                .city(city)
                .zone(zone)
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .imageUrl(imageUrl)
                .severity(Pothole.Severity.valueOf(severity))
                .severityScore(sevScore.floatValue())
                .confidenceScore(confidence.floatValue())
                .bboxWidth(bboxW)
                .bboxHeight(bboxH)
                .detectedBy(Pothole.DetectedBy.citizen)
                .status(Pothole.Status.pending)
                .priorityScore(priority)
                .upvoteCount(0)
                .roadType(Pothole.RoadType.valueOf(
                        req.getRoadType() != null ? req.getRoadType() : "unknown"))
                .reportedBy(user)
                .build();

        potholeRepository.save(pothole);

        // 9. Auto assign to zone authority
        authorityRepository.findFirstByZoneIdAndIsActiveTrue(zone.getId())
                .ifPresent(authority -> {
                    pothole.setAssignedTo(authority);
                    potholeRepository.save(pothole);

                    // Save notification
                    Notification notif = Notification.builder()
                            .pothole(pothole)
                            .authority(authority)
                            .type(Notification.Type.new_report)
                            .sentTo(authority.getEmail())
                            .isSent(false)
                            .build();
                    notificationRepository.save(notif);

                    // Send email to authority
                    if (authority.getEmail() != null && !authority.getEmail().isEmpty()) {
                        emailService.sendNewPotholeAlert(
                                authority.getEmail(),
                                zone.getZoneName(),
                                severity,
                                req.getLatitude(),
                                req.getLongitude()
                        );
                    }
                });

        log.info("Pothole reported: ID={}, City={}, Severity={}", pothole.getId(), city.getName(), severity);
        return mapToResponse(pothole);
    }

    // ── GET ALL FOR MAP ───────────────────────────────────────
    public List<MapPotholeResponse> getPotholesForMap(Integer cityId) {
        List<Pothole> potholes = potholeRepository.findByCityIdOrderByPriority(cityId);
        return potholes.stream().map(p -> MapPotholeResponse.builder()
                .id(p.getId())
                .latitude(p.getLatitude())
                .longitude(p.getLongitude())
                .severity(p.getSeverity().name())
                .status(p.getStatus().name())
                .upvoteCount(p.getUpvoteCount())
                .imageUrl(p.getImageUrl())
                .zoneName(p.getZone().getZoneName())
                .build()
        ).collect(Collectors.toList());
    }

    // ── GET SINGLE POTHOLE ────────────────────────────────────
    public PotholeResponse getPotholeById(Integer id) {
        Pothole p = potholeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pothole not found"));
        return mapToResponse(p);
    }

    // ── GET BY CITY ───────────────────────────────────────────
    public List<PotholeResponse> getPotholesByCity(Integer cityId) {
        return potholeRepository.findByCityId(cityId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ── GET BY ZONE ───────────────────────────────────────────
    public List<PotholeResponse> getPotholesByZone(Integer zoneId) {
        return potholeRepository.findByZoneId(zoneId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ── UPVOTE ────────────────────────────────────────────────
    public Map<String, Object> upvotePothole(Integer potholeId, String userEmail) {
        Pothole pothole = potholeRepository.findById(potholeId)
                .orElseThrow(() -> new RuntimeException("Pothole not found"));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (upvoteRepository.existsByPotholeIdAndUserId(potholeId, user.getId())) {
            return Map.of("message", "Already upvoted", "upvoteCount", pothole.getUpvoteCount());
        }

        Upvote upvote = Upvote.builder().pothole(pothole).user(user).build();
        upvoteRepository.save(upvote);

        pothole.setUpvoteCount(pothole.getUpvoteCount() + 1);
        // Recalculate priority
        pothole.setPriorityScore(calculatePriority(
                pothole.getSeverity().name(),
                pothole.getUpvoteCount(),
                pothole.getRoadType().name()
        ));
        potholeRepository.save(pothole);

        return Map.of("message", "Upvoted successfully", "upvoteCount", pothole.getUpvoteCount());
    }

    // ── UPDATE STATUS (Authority) ─────────────────────────────
    public PotholeResponse updateStatus(Integer potholeId, StatusUpdateRequest req) {
        Pothole pothole = potholeRepository.findById(potholeId)
                .orElseThrow(() -> new RuntimeException("Pothole not found"));

        Pothole.Status newStatus = Pothole.Status.valueOf(req.getStatus());
        pothole.setStatus(newStatus);
        if (req.getNotes() != null) pothole.setNotes(req.getNotes());
        potholeRepository.save(pothole);

        // Notify citizen
        if (pothole.getReportedBy() != null && pothole.getReportedBy().getEmail() != null) {
            emailService.sendStatusUpdateToUser(
                    pothole.getReportedBy().getEmail(),
                    pothole.getReportedBy().getName(),
                    potholeId,
                    req.getStatus()
            );
        }

        log.info("Pothole {} status updated to {}", potholeId, req.getStatus());
        return mapToResponse(pothole);
    }

    // ── MY REPORTS ────────────────────────────────────────────
    public List<PotholeResponse> getMyReports(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return potholeRepository.findAll().stream()
                .filter(p -> p.getReportedBy() != null &&
                        p.getReportedBy().getId().equals(user.getId()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── HELPER: Priority Score ────────────────────────────────
    private int calculatePriority(String severity, int upvotes, String roadType) {
        int score = upvotes * 2;
        score += switch (severity) {
            case "high"   -> 40;
            case "medium" -> 20;
            default       -> 5;
        };
        score += switch (roadType) {
            case "highway"   -> 30;
            case "main_road" -> 20;
            case "internal"  -> 10;
            default          -> 0;
        };
        return score;
    }

    // ── HELPER: Map to Response ───────────────────────────────
    private PotholeResponse mapToResponse(Pothole p) {
        return PotholeResponse.builder()
                .id(p.getId())
                .city(p.getCity().getName())
                .zoneName(p.getZone().getZoneName())
                .zonePhone(p.getZone().getPhone())
                .wardNumber(p.getWard() != null ? p.getWard().getWardNumber() : null)
                .wardName(p.getWard() != null ? p.getWard().getWardName() : null)
                .latitude(p.getLatitude())
                .longitude(p.getLongitude())
                .imageUrl(p.getImageUrl())
                .severity(p.getSeverity().name())
                .severityScore(p.getSeverityScore())
                .confidenceScore(p.getConfidenceScore())
                .status(p.getStatus().name())
                .upvoteCount(p.getUpvoteCount())
                .priorityScore(p.getPriorityScore())
                .roadType(p.getRoadType() != null ? p.getRoadType().name() : null)
                .detectedBy(p.getDetectedBy().name())
                .reportedByName(p.getReportedBy() != null ? p.getReportedBy().getName() : "Auto")
                .assignedOfficer(p.getAssignedTo() != null ? p.getAssignedTo().getName() : null)
                .createdAt(p.getCreatedAt())
                .build();
    }
}
