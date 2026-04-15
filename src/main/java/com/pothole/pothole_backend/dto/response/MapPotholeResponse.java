package com.pothole.pothole_backend.dto.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MapPotholeResponse {
    private Integer id;
    private Double latitude;
    private Double longitude;
    private String severity;
    private String status;
    private Integer upvoteCount;
    private String imageUrl;
    private String zoneName;
}
