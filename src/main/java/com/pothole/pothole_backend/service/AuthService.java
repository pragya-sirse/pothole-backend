package com.pothole.pothole_backend.service;


import com.pothole.pothole_backend.dto.request.LoginRequest;
import com.pothole.pothole_backend.dto.request.RegisterRequest;
import com.pothole.pothole_backend.dto.response.AuthResponse;
import com.pothole.pothole_backend.model.City;
import com.pothole.pothole_backend.model.User;
import com.pothole.pothole_backend.model.Ward;
import com.pothole.pothole_backend.repository.CityRepository;
import com.pothole.pothole_backend.repository.UserRepository;
import com.pothole.pothole_backend.repository.WardRepository;
import com.pothole.pothole_backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final WardRepository wardRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        City city = null;
        if (req.getCityId() != null) {
            city = cityRepository.findById(req.getCityId())
                    .orElseThrow(() -> new RuntimeException("City not found"));
        }

        Ward ward = null;
        if (req.getWardId() != null) {
            ward = wardRepository.findById(req.getWardId())
                    .orElseThrow(() -> new RuntimeException("Ward not found"));
        }

        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .phone(req.getPhone())
                .city(city)
                .ward(ward)
                .role(User.Role.citizen)
                .isVerified(true)
                .build();

        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
