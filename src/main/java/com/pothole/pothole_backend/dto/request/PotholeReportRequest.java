package com.pothole.pothole_backend.dto.request;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PotholeReportRequest {
    @NotNull(message = "City ID is required")
    private Integer cityId;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    private String description;
    private String roadType;

    private Integer zoneId;
    private String  issueType;
    private String  severity;
}
