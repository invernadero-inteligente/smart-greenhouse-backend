package com.greenhouse.smart_backend.modules.users.service;

import com.greenhouse.smart_backend.modules.users.dto.request.AuthLoginRequest;
import com.greenhouse.smart_backend.modules.users.dto.request.AuthRegisterRequest;
import com.greenhouse.smart_backend.modules.users.dto.request.UserCreateRequest;
import com.greenhouse.smart_backend.modules.users.dto.request.UserStatusUpdateRequest;
import com.greenhouse.smart_backend.modules.users.dto.request.UserUpdateRequest;
import com.greenhouse.smart_backend.modules.users.dto.response.AuthResponse;
import com.greenhouse.smart_backend.modules.users.dto.response.UserResponseDTO;

import java.util.List;

public interface UserService {

    AuthResponse register(AuthRegisterRequest request);

    AuthResponse authenticate(AuthLoginRequest request);

    List<UserResponseDTO> listUsers();

    UserResponseDTO createUser(UserCreateRequest request);

    UserResponseDTO getUserById(Long id);

    UserResponseDTO updateUser(Long id, UserUpdateRequest request, Long currentUserId);

    UserResponseDTO updateUserStatus(Long id, UserStatusUpdateRequest request, Long currentUserId);

}
