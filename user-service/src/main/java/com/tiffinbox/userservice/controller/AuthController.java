package com.tiffinbox.userservice.controller;

import com.tiffinbox.userservice.dto.LoginRequest;
import com.tiffinbox.userservice.dto.RegisterRequest;
import com.tiffinbox.userservice.dto.UserResponse;
import com.tiffinbox.userservice.entity.User;
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

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    /**
     * Phase 2: validates credentials and returns the user.
     * Phase 3 will wrap this with a signed JWT.
     */
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.login(request);
        return ResponseEntity.ok(UserResponse.from(user));
    }
}
