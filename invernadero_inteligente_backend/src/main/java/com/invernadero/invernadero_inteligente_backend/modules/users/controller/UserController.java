package com.invernadero.invernadero_inteligente_backend.modules.users.controller;

import com.invernadero.invernadero_inteligente_backend.modules.users.dto.request.UserCreateRequest;
import com.invernadero.invernadero_inteligente_backend.modules.users.dto.request.UserStatusUpdateRequest;
import com.invernadero.invernadero_inteligente_backend.modules.users.dto.request.UserUpdateRequest;
import com.invernadero.invernadero_inteligente_backend.modules.users.dto.response.UserResponseDTO;
import com.invernadero.invernadero_inteligente_backend.modules.users.service.UserService;
import com.invernadero.invernadero_inteligente_backend.shared.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> listUsers() {
        return ResponseEntity.ok(userService.listUsers());
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        return ResponseEntity.ok(userService.updateUser(id, request, currentUser.getId()));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<UserResponseDTO> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        return ResponseEntity.ok(userService.updateUserStatus(id, request, currentUser.getId()));
    }
}
