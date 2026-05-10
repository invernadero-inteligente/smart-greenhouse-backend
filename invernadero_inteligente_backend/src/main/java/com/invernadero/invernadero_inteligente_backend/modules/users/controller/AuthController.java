package com.invernadero.invernadero_inteligente_backend.modules.users.controller;

import com.invernadero.invernadero_inteligente_backend.modules.users.dto.request.AuthLoginRequest;
import com.invernadero.invernadero_inteligente_backend.modules.users.dto.request.AuthRegisterRequest;
import com.invernadero.invernadero_inteligente_backend.modules.users.dto.response.AuthResponse;
import com.invernadero.invernadero_inteligente_backend.modules.users.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRegisterRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthLoginRequest request) {
        return ResponseEntity.ok(userService.authenticate(request));
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("Auth ping OK");
    }
}
