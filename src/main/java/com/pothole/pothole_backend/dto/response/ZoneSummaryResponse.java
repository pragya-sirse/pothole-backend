package com.pothole.pothole_backend.dto.response;

import lombok.*;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class ZoneSummaryResponse {
    private String city;
    private String zoneName;
    private String contact;
    private Long totalPotholes;
    private Long highSeverity;
    private Long mediumSeverity;
    private Long lowSeverity;
    private Long pending;
    private Long inProgress;
    private Long completed;
}
