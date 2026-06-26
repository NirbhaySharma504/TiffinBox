package com.tiffinbox.userservice.controller;

import com.tiffinbox.userservice.dto.UserResponse;
import com.tiffinbox.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(UserResponse.from(userService.getById(id)));
    }

    /**
     * Returns the caller's own profile based on the X-User-Id header the Gateway
     * forwards after validating the JWT (wired up in Phase 4).
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(UserResponse.from(userService.getById(userId)));
    }
}
