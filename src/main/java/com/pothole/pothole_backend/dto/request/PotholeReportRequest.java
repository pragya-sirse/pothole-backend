package com.pothole.pothole_backend.dto.request;


import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PotholeReportRequest {
    @NotNull(message = "City ID is required")
    private Integer cityId;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    private String description;
    private String roadType;
}
