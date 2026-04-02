package com.wom.milkmgmt.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @Size(min = 3, max = 50)
    private String username;      // optional

    @Email(message = "Invalid email format")
    private String email;         // optional

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;      // optional

    @Size(max = 15)
    private String phoneNumber;   // optional

    private Set<String> roles;    // optional

    private Boolean enabled;      // optional
}
