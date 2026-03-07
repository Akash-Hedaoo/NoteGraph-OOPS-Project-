package com.notegraph.backend.controller;

import com.notegraph.backend.dto.AuthRequest;
import com.notegraph.backend.model.User;
import com.notegraph.backend.repository.UserRepository;
import com.notegraph.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public org.springframework.http.ResponseEntity<?> register(@RequestBody AuthRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty() || request.getPassword() == null
                || request.getPassword().trim().isEmpty()) {
            return org.springframework.http.ResponseEntity.badRequest().body("Username and password cannot be empty");
        }
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return org.springframework.http.ResponseEntity.badRequest().body("Error: Username is already taken!");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getUsername() + "@example.com"); // Dummy email as it's required by the model
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");

        userRepository.save(user);
        return org.springframework.http.ResponseEntity.ok("User registered successfully!");
    }

    @PostMapping("/login")
    public org.springframework.http.ResponseEntity<?> login(@RequestBody AuthRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty() || request.getPassword() == null
                || request.getPassword().trim().isEmpty()) {
            return org.springframework.http.ResponseEntity.badRequest().body("Username and password cannot be empty");
        }
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());

        if (userOpt.isPresent() && passwordEncoder.matches(request.getPassword(), userOpt.get().getPassword())) {
            return org.springframework.http.ResponseEntity.ok(jwtUtil.generateToken(request.getUsername()));
        }

        return org.springframework.http.ResponseEntity.badRequest().body("Error: Invalid username or password");
    }
}
