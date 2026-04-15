package com.pothole.pothole_backend.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PotholeResponse {
    private Integer id;
    private String city;
    private String zoneName;
    private String zonePhone;
    private Integer wardNumber;
    private String wardName;
    private Double latitude;
    private Double longitude;
    private String imageUrl;
    private String severity;
    private Float severityScore;
    private Float confidenceScore;
    private String status;
    private Integer upvoteCount;
    private Integer priorityScore;
    private String roadType;
    private String detectedBy;
    private String reportedByName;
    private String assignedOfficer;
    private LocalDateTime createdAt;
}