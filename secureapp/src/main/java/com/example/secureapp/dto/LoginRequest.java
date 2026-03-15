package com.example.secureapp.dto;

import lombok.Data;

/**
 * Clean Code: Data Transfer Object (DTO)
 * This is used to capture the login credentials from the JSON body
 * in the POST /api/login request.
 */
@Data
public class LoginRequest {
    private String username; // Maps to the user's email
    private String password;
}