package com.wom.milkmgmt.controller;

import com.wom.milkmgmt.configuration.JwtUtils;
import com.wom.milkmgmt.entity.User;
import com.wom.milkmgmt.model.AuthResponse;
import com.wom.milkmgmt.model.LoginRequest;
import com.wom.milkmgmt.model.RegisterRequest;
import com.wom.milkmgmt.repository.UserRepository;
import com.wom.milkmgmt.service.AuthService;
import com.wom.milkmgmt.service.impl.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authManager;
    private final UserRepository userRepository;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        // Step 1 - authenticate (throws BadCredentialsException if wrong)
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // Step 2 - fetch User entity from DB ← THIS is where you get User object
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Step 3 - generate JWT token
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtUtils.generateToken(userDetails);

        // Step 4 - extract roles from User object
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

        return AuthResponse.builder()
                .status(200)
                .message("Login successful")
                .token(token)
                .id(user.getId().toString())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roles)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }
    @GetMapping("/verify-hash")
    @PreAuthorize("hasRole('ADMIN')")
    public String verifyHash(@RequestParam String password, @RequestParam String hash) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        boolean matches = encoder.matches(password, hash);
        return matches ? "✅ Password matches!" : "❌ Password does not match!";
    }

    @GetMapping("/generate-hash")
    @PreAuthorize("hasRole('ADMIN')")
    public String generateHash(@RequestParam String password) {
        return new BCryptPasswordEncoder().encode(password);
    }


}
