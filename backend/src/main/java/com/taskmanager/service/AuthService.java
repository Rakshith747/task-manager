package com.taskmanager.service;

import com.taskmanager.dto.AuthDTOs;
import com.taskmanager.model.User;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthDTOs.AuthResponse signup(AuthDTOs.SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User.Role role = User.Role.MEMBER;
        if ("ADMIN".equalsIgnoreCase(request.getRole())) {
            role = User.Role.ADMIN;
        }

        User user = new User(
            request.getName(),
            request.getEmail(),
            passwordEncoder.encode(request.getPassword()),
            role
        );

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthDTOs.AuthResponse(token, user.getName(), user.getEmail(), user.getRole().name(), user.getId());
    }

    public AuthDTOs.AuthResponse login(AuthDTOs.LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthDTOs.AuthResponse(token, user.getName(), user.getEmail(), user.getRole().name(), user.getId());
    }
}
