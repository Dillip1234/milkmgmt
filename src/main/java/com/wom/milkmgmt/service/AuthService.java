package com.wom.milkmgmt.service;

import com.wom.milkmgmt.configuration.JwtUtils;
import com.wom.milkmgmt.entity.Role;
import com.wom.milkmgmt.entity.RoleName;
import com.wom.milkmgmt.entity.User;
import com.wom.milkmgmt.exception.DuplicateResourceException;
import com.wom.milkmgmt.model.LoginRequest;
import com.wom.milkmgmt.model.RegisterRequest;
import com.wom.milkmgmt.repository.RoleRepository;
import com.wom.milkmgmt.repository.UserRepository;
import com.wom.milkmgmt.service.impl.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    public String login(LoginRequest request) {
        // Throws BadCredentialsException if wrong
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        return jwtUtils.generateToken(userDetails);
    }

    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername()))
            throw new DuplicateResourceException("Username already taken");

        if (userRepository.existsByEmail(request.getEmail()))
            throw new DuplicateResourceException("Email already in use");

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setCreatedAt(LocalDateTime.now().toString()); // e.g. "2026-03-14T16:32:39"
        user.setPassword(passwordEncoder.encode(request.getPassword())); // BCrypt hash
        user.setEnabled(true);
        // ← Role assignment logic
        Set<Role> roles = new HashSet<>();
        // Assign default role
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            // no roles provided → default to ROLE_USER
            Role defaultRole = roleRepository.findByName(RoleName.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("ROLE_USER not found in DB"));
            roles.add(defaultRole);
        } else {
            // map each string to Role entity
            request.getRoles().forEach(roleName -> {
                switch (roleName.toUpperCase()) {
                    case "ADMIN" -> {
                        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found in DB"));
                        roles.add(adminRole);
                    }
                    case "MODERATOR" -> {
                        Role modRole = roleRepository.findByName(RoleName.ROLE_MODERATOR)
                                .orElseThrow(() -> new RuntimeException("ROLE_MODERATOR not found in DB"));
                        roles.add(modRole);
                    }
                    default -> {
                        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("ROLE_USER not found in DB"));
                        roles.add(userRole);
                    }
                }
            });

            user.setRoles(roles);
            userRepository.save(user);
        }
    }
}