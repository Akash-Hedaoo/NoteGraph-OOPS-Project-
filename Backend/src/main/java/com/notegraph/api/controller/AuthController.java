package com.notegraph.api.controller;

import com.notegraph.api.dto.AuthRequest;
import com.notegraph.api.dto.AuthResponse;
import com.notegraph.api.dto.RegisterRequest;
import com.notegraph.api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<java.util.Map<String, String>> handleException(Exception e) {
        return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
