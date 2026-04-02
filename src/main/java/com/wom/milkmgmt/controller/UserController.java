package com.wom.milkmgmt.controller;

import com.wom.milkmgmt.model.UpdateUserRequest;
import com.wom.milkmgmt.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
//@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "Users fetched successfully",
                "data", userService.getAllUsers(),
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "User fetched successfully",
                "data", userService.getUserById(id),
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long id,
                                        @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "User updated successfully",
                "data", userService.updateUser(id, request),
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "User deleted successfully",
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
