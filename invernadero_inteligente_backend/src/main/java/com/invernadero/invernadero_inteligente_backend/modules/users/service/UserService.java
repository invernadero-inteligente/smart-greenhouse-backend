package com.invernadero.invernadero_inteligente_backend.modules.users.service;

import com.invernadero.invernadero_inteligente_backend.modules.users.dto.request.AuthLoginRequest;
import com.invernadero.invernadero_inteligente_backend.modules.users.dto.request.AuthRegisterRequest;
import com.invernadero.invernadero_inteligente_backend.modules.users.dto.request.UserCreateRequest;
import com.invernadero.invernadero_inteligente_backend.modules.users.dto.request.UserStatusUpdateRequest;
import com.invernadero.invernadero_inteligente_backend.modules.users.dto.request.UserUpdateRequest;
import com.invernadero.invernadero_inteligente_backend.modules.users.dto.response.AuthResponse;
import com.invernadero.invernadero_inteligente_backend.modules.users.dto.response.UserResponseDTO;

import java.util.List;

public interface UserService {

    AuthResponse register(AuthRegisterRequest request);

    AuthResponse authenticate(AuthLoginRequest request);

    List<UserResponseDTO> listUsers();

    UserResponseDTO createUser(UserCreateRequest request);

    UserResponseDTO updateUser(Long id, UserUpdateRequest request, Long currentUserId);

    UserResponseDTO updateUserStatus(Long id, UserStatusUpdateRequest request, Long currentUserId);

}
