package com.bank.authservice.service;

import com.bank.authservice.dto.AuthResponse;
import com.bank.authservice.dto.EmailRequest;
import com.bank.authservice.dto.LoginRequest;
import com.bank.authservice.dto.RegisterRequest;
import com.bank.authservice.entity.Role;
import com.bank.authservice.entity.User;
import com.bank.authservice.feign.EmailClient;
import com.bank.authservice.repository.UserRepository;
import com.bank.authservice.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthService {

    @Autowired
    private EmailClient emailClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;


    public AuthResponse register(RegisterRequest request) {

        System.out.println("STEP 0");

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);

        System.out.println("STEP 1");

        userRepository.save(user);

        System.out.println("STEP 2");

        EmailRequest emailRequest = new EmailRequest();

        emailRequest.setTo(user.getEmail());
        emailRequest.setSubject("Welcome To MyBank");
        emailRequest.setBody(
                "Hello " + user.getName() +
                        ", your account has been created successfully."
        );

        System.out.println("STEP 3");

        emailClient.sendEmail(emailRequest);

        System.out.println("STEP 4");

        String token =
                jwtUtil.generateToken(
                        user.getEmail(),
                        user.getRole().name());

        System.out.println("STEP 5");

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new RuntimeException("Invalid password");
        }

        String token =
                jwtUtil.generateToken(
                        user.getEmail(),
                        user.getRole().name());

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}