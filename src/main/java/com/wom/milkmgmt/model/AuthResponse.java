package com.wom.milkmgmt.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class AuthResponse {
    private int status;
    private String message;
    private String token;
    private String username;
    private String id;
    private String email;
    private List<String> roles;
    private String timestamp;
}
