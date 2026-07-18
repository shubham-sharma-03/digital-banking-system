package com.bank.authservice.service;

import com.bank.authservice.dto.AuthResponse;
import com.bank.authservice.dto.LoginRequest;
import com.bank.authservice.dto.RegisterRequest;
import com.bank.authservice.entity.Role;
import com.bank.authservice.entity.User;
import com.bank.authservice.repository.UserRepository;
import com.bank.authservice.security.JwtUtil;
import jakarta.annotation.PostConstruct;
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

    // INSERT DEFAULT DATA ON STARTUP
    @PostConstruct
    public void initDefaultUsers() {
        if (!userRepository.existsByEmail("shubham@gmail.com")) {
            User user = new User();
            user.setName("Shubham Sharma");
            user.setEmail("shubham@gmail.com");
            user.setPassword(passwordEncoder.encode("123456"));
            user.setRole(Role.USER);
            userRepository.save(user);
            System.out.println("Default user created: shubham@gmail.com");
        }

        if (!userRepository.existsByEmail("admin@mybank.com")) {
            User admin = new User();
            admin.setName("Admin User");
            admin.setEmail("admin@mybank.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
            System.out.println("Default admin created: admin@mybank.com");
        }
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return new AuthResponse(token, user.getId(), user.getEmail(), user.getName(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return new AuthResponse(token, user.getId(), user.getEmail(), user.getName(), user.getRole().name());
    }
}