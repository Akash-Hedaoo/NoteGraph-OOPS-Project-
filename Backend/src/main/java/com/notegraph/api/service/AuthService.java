package com.notegraph.api.service;

import com.notegraph.api.domain.User;
import com.notegraph.api.domain.Workspace;
import com.notegraph.api.dto.AuthRequest;
import com.notegraph.api.dto.AuthResponse;
import com.notegraph.api.dto.RegisterRequest;
import com.notegraph.api.dto.UserDto;
import com.notegraph.api.repository.UserRepository;
import com.notegraph.api.repository.WorkspaceRepository;
import com.notegraph.api.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .build();

        User savedUser = userRepository.save(user);

        // Automatically create a default personal workspace for new users
        Workspace defaultWorkspace = Workspace.builder()
                .name("Personal Workspace")
                .owner(savedUser)
                .build();
        workspaceRepository.save(defaultWorkspace);

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPasswordHash(), new ArrayList<>());
        String token = jwtUtil.generateToken(userDetails);

        return new AuthResponse(token, UserDto.fromEntity(savedUser), defaultWorkspace.getId());
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        // Fetch user's workspaces
        java.util.List<Workspace> workspaces = workspaceRepository.findByOwnerId(user.getId());
        java.util.UUID workspaceId = workspaces.isEmpty() ? null : workspaces.get(0).getId();

        return new AuthResponse(token, UserDto.fromEntity(user), workspaceId);
    }
}
