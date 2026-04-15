package com.pothole.pothole_backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class StatusUpdateRequest {
    @NotBlank(message = "Status is required")
    private String status; // pending, in_progress, completed, rejected

    private String notes;
    private String contractorName;
}
