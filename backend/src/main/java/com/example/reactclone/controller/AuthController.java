package com.example.reactclone.controller;

import com.example.reactclone.dto.AuthenticationResponse;
import com.example.reactclone.dto.LoginRequest;
import com.example.reactclone.dto.RegisterRequest;
import com.example.reactclone.exception.RedditException;
import com.example.reactclone.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestBody RegisterRequest registerRequest
    ) {
        try {
            this.authService.register(registerRequest);
            return ResponseEntity
                    .ok().body("User registered successfully");
        } catch (RedditException e) {
            return ResponseEntity
                    .internalServerError().body(e.getLocalizedMessage());
        }
    }

    @GetMapping("/accountVerification/{token}")
    public ResponseEntity<String> verifyAccount(
            @PathVariable String token
    ) {
        this.authService.verifyAccount(token);
        return ResponseEntity
                .ok().body("Account Verified Successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @RequestBody LoginRequest loginRequest
    ) {
        return ResponseEntity.ok()
                .body(this.authService.login(loginRequest));
    }
}
