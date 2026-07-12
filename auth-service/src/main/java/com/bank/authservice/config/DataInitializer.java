package com.bank.authservice.config;

import com.bank.authservice.entity.Role;
import com.bank.authservice.entity.User;
import com.bank.authservice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner init(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByEmail("shubham@gmail.com").isEmpty()) {
                User user = new User();
                user.setEmail("shubham@gmail.com");
                user.setPassword(passwordEncoder.encode("123456"));
                user.setName("Shubham Sharma");
                user.setRole(Role.valueOf("USER"));
                userRepository.save(user);
                System.out.println("Default user created: shubham@gmail.com / 123456");
            }
        };
    }
}