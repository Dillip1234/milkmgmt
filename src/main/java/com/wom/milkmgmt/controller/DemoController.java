package com.wom.milkmgmt.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DemoController {

    @GetMapping("/user/hello")
    @PreAuthorize("hasRole('USER')")
    public String userHello() {
        return "Hello, User!";
    }

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard() {
        return "Welcome, Admin!";
    }

    @GetMapping("/mod/panel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public String modPanel() {
        return "Moderator Panel";
    }
}
