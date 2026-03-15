package com.example.secureapp.controller;

import com.example.secureapp.dto.LoginRequest;
import com.example.secureapp.model.User;
import com.example.secureapp.repository.UserRepository;
import com.example.secureapp.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final MfaService mfaService;

    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody List<User> users) {
        userService.register(users);
        return ResponseEntity.ok("Registered successfully.");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest req) {
        if (userService.validateLogin(req.getUsername(), req.getPassword())) {
            return ResponseEntity.ok("Credentials valid. Submit MFA code next.");
        }
        return ResponseEntity.status(401).body("Invalid credentials.");
    }

    @Transactional
    @GetMapping(value = "/mfa/setup", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> setupMfa(@RequestParam String email) throws Exception {
        User user = userService.getByEmail(email);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Check if secret key is missing and generate/save it
        if (user.getSecretKey() == null || user.getSecretKey().isEmpty()) {
            String secret = mfaService.generateNewSecret();
            user.setSecretKey(secret);
            // Now this works because userRepository is injected above
            userRepository.save(user);
        }

        byte[] qrCode = mfaService.generateQrCode(user.getSecretKey(), user.getEmail());
        return ResponseEntity.ok(qrCode);
    }
    // 1. Secure Access via HTTPS (handled in application.properties/keystore)
    @PostMapping("/mfa/verify")
    public ResponseEntity<String> verifyMfa(@RequestParam String email, @RequestParam String code, HttpSession session) {
        // Protection against SQL Injection (using Repository instead of raw SQL)
        User user = userService.getByEmail(email);

        if (user == null || user.getSecretKey() == null) {
            return ResponseEntity.status(400).body("MFA not set up for this user.");
        }
        //Protection against Phishing (Multi-Factor verification)
        if (mfaService.verifyCode(user.getSecretKey(), code)) {
            // Usually, you would mark MFA as enabled in the DB here
            user.setMfaEnabled(true);
            userRepository.save(user);

            //  Mark the Session (Current Login)
            session.setAttribute("mfa_verified", true);
            session.setAttribute("user_email", email);

            return ResponseEntity.ok("MFA Verified. Access Granted.");
        }
        return ResponseEntity.status(401).body("Access Denied");
    }

    @GetMapping("/dashboard")
    public ResponseEntity<String> getDashboard(HttpSession session) {
        // Look for the "stamp" we just created
        Boolean isVerified = (Boolean) session.getAttribute("mfa_verified");

        if (isVerified != null && isVerified) {
            String email = (String) session.getAttribute("user_email");
            return ResponseEntity.ok("Welcome to the Secure Dashboard, " + email);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("MFA required for this session.");
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}