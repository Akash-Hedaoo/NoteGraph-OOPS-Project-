package com.notegraph.backend.service;

import com.notegraph.backend.model.User;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    public String generateToken(User user) {
        return "dummy-token-for-" + user.getUsername();
    }

    public boolean validateToken(String token) {
        return true;
    }
}
