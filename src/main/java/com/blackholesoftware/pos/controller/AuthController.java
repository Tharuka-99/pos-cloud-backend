package com.blackholesoftware.pos.controller;

import com.blackholesoftware.pos.dto.ApiResponse;
import com.blackholesoftware.pos.dto.AuthResponseData;
import com.blackholesoftware.pos.dto.LoginRequest;
import com.blackholesoftware.pos.entity.User;
import com.blackholesoftware.pos.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void initDefaultUser() {
        try {
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setFullName("Suneri Admin");
                admin.setRole(User.Role.ADMIN);
                admin.setIsActive(true);
                admin.setCreatedAt(LocalDateTime.now());

                userRepository.save(admin);
                System.out.println(">>> Seed Admin User Created in SQLite: (admin / admin123)");
            }
        } catch (Exception e) {
            System.err.println(">>> Admin initialization skipped/failed: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseData>> login(@RequestBody LoginRequest request) {
        Optional<User> userOptional = userRepository.findByUsername(request.getUsername());

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (Boolean.TRUE.equals(user.getIsActive()) && passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                AuthResponseData responseData = new AuthResponseData(
                        user.getId(),
                        user.getUsername(),
                        user.getFullName(),
                        user.getRole().name()
                );
                return ResponseEntity.ok(new ApiResponse<>(true, "Login Successful", responseData));
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(false, "Invalid Username or Password!", null));
    }
}