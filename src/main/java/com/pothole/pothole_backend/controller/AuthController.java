package com.pothole.pothole_backend.controller;

import com.pothole.pothole_backend.dto.request.LoginRequest;
import com.pothole.pothole_backend.dto.request.RegisterRequest;
import com.pothole.pothole_backend.dto.response.ApiResponse;
import com.pothole.pothole_backend.dto.response.AuthResponse;
import com.pothole.pothole_backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Registered!", authService.register(req)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Login success!", authService.login(req)));
    }

    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> test() {
        return ResponseEntity.ok(ApiResponse.success("Backend is running!", "OK"));
    }
}