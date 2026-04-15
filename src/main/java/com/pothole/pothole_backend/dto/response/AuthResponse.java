package com.pothole.pothole_backend.dto.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Integer userId;
    private String name;
    private String email;
    private String role;
}
