package com.wom.milkmgmt.service;

import com.wom.milkmgmt.entity.Role;
import com.wom.milkmgmt.entity.RoleName;
import com.wom.milkmgmt.entity.User;
import com.wom.milkmgmt.exception.DuplicateResourceException;
import com.wom.milkmgmt.model.UpdateUserRequest;
import com.wom.milkmgmt.model.UserResponse;
import com.wom.milkmgmt.repository.RoleRepository;
import com.wom.milkmgmt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // UPDATE USER
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // update only fields that are provided
        if (request.getUsername() != null) {
            if (userRepository.existsByUsername(request.getUsername()))
                throw new DuplicateResourceException("Username already taken");
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null) {
            if (userRepository.existsByEmail(request.getEmail()))
                throw new DuplicateResourceException("Email already in use");
            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            request.getRoles().forEach(roleName -> {
                switch (roleName.toUpperCase()) {
                    case "ADMIN" -> roles.add(roleRepository.findByName(RoleName.ROLE_ADMIN)
                            .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found")));
                    case "MODERATOR" -> roles.add(roleRepository.findByName(RoleName.ROLE_MODERATOR)
                            .orElseThrow(() -> new RuntimeException("ROLE_MODERATOR not found")));
                    default -> roles.add(roleRepository.findByName(RoleName.ROLE_USER)
                            .orElseThrow(() -> new RuntimeException("ROLE_USER not found")));
                }
            });
            user.setRoles(roles);
        }

        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        userRepository.save(user);
        return mapToResponse(user);
    }

    // DELETE USER
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        userRepository.delete(user);
    }

    // GET USER BY ID
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return mapToResponse(user);
    }

    // GET ALL USERS
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // map User entity → UserResponse
    private UserResponse mapToResponse(User user) {
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .enabled(user.isEnabled())
                .roles(roles)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
