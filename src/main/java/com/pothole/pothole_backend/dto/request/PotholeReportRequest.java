package com.pothole.pothole_backend.dto.request;


import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PotholeReportRequest {
    @NotNull(message = "City ID required")
    private Integer cityId;

    @NotNull(message = "Latitude required")
    private Double latitude;

    @NotNull(message = "Longitude required")
    private Double longitude;

    private String description;
    private String roadType;
    // image comes as MultipartFile in controller separately
}
