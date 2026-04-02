package com.wom.milkmgmt.model;

import lombok.Data;

@Data
public class LoginResponse {

    private String status;
    private Long userId;
    private String message;

    public LoginResponse(String status, Long userId, String message) {
        this.status = status;
        this.userId = userId;
        this.message = message;
    }
}
