package com.tiffinbox.userservice.controller;

import com.tiffinbox.userservice.dto.AuthResponse;
import com.tiffinbox.userservice.dto.LoginRequest;
import com.tiffinbox.userservice.dto.RegisterRequest;
import com.tiffinbox.userservice.dto.UserResponse;
import com.tiffinbox.userservice.entity.User;
import com.tiffinbox.userservice.service.JwtService;
import com.tiffinbox.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    /** Registers the user and returns a signed JWT so the client is logged in immediately. */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request);
        String token = jwtService.generateToken(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AuthResponse.of(token, UserResponse.from(user)));
    }

    /** Validates credentials and returns a signed JWT plus the user profile. */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.login(request);
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(AuthResponse.of(token, UserResponse.from(user)));
    }
}
