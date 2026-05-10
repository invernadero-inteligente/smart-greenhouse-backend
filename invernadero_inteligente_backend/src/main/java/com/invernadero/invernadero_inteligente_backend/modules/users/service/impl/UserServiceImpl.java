package com.invernadero.invernadero_inteligente_backend.modules.users.service.impl;

import com.invernadero.invernadero_inteligente_backend.modules.users.dto.request.AuthLoginRequest;
import com.invernadero.invernadero_inteligente_backend.modules.users.dto.request.AuthRegisterRequest;
import com.invernadero.invernadero_inteligente_backend.modules.users.dto.request.UserCreateRequest;
import com.invernadero.invernadero_inteligente_backend.modules.users.dto.request.UserStatusUpdateRequest;
import com.invernadero.invernadero_inteligente_backend.modules.users.dto.request.UserUpdateRequest;
import com.invernadero.invernadero_inteligente_backend.modules.users.dto.response.AuthResponse;
import com.invernadero.invernadero_inteligente_backend.modules.users.dto.response.UserResponseDTO;
import com.invernadero.invernadero_inteligente_backend.modules.users.model.User;
import com.invernadero.invernadero_inteligente_backend.modules.users.model.UserRole;
import com.invernadero.invernadero_inteligente_backend.modules.users.repository.UserRepository;
import com.invernadero.invernadero_inteligente_backend.modules.users.service.UserService;
import com.invernadero.invernadero_inteligente_backend.shared.exceptions.ResourceNotFoundException;
import com.invernadero.invernadero_inteligente_backend.shared.exceptions.ValidationException;
import com.invernadero.invernadero_inteligente_backend.shared.security.JwtTokenProvider;
import com.invernadero.invernadero_inteligente_backend.shared.security.UserPrincipal;
import com.invernadero.invernadero_inteligente_backend.shared.utils.ValidationUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse register(AuthRegisterRequest request) {
        ValidationUtil.validateNotEmpty(request.getFullName(), "fullName");
        ValidationUtil.validateEmail(request.getEmail());
        ValidationUtil.validatePassword(request.getPassword());

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException("confirmPassword", "Las contraseñas no coinciden");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("email", "El correo electrónico ya está registrado");
        }

        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.OPERATOR)
                .active(true)
                .build();

        User saved = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(UserPrincipal.create(saved));

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationMs())
                .userId(saved.getId())
                .email(saved.getEmail())
                .role(saved.getRole())
                .build();
    }

    @Override
    public AuthResponse authenticate(AuthLoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail().trim().toLowerCase(), request.getPassword()));
        } catch (DisabledException ex) {
            throw new ValidationException("credentials", "Correo o contraseña incorrectos");
        } catch (BadCredentialsException ex) {
            throw new ValidationException("credentials", "Correo o contraseña incorrectos");
        }

        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new ValidationException("credentials", "Correo o contraseña incorrectos"));

        String token = jwtTokenProvider.generateToken(UserPrincipal.create(user));
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationMs())
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    public List<UserResponseDTO> listUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponseDTO createUser(UserCreateRequest request) {
        ValidationUtil.validateNotEmpty(request.getFullName(), "fullName");
        ValidationUtil.validateEmail(request.getEmail());
        ValidationUtil.validatePassword(request.getPassword());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("email", "El correo electrónico ya está registrado");
        }

        UserRole role = parseRole(request.getRole());

        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .active(true)
                .build();

        return toResponseDTO(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponseDTO updateUser(Long id, UserUpdateRequest request, Long currentUserId) {
        ValidationUtil.validateNotEmpty(request.getFullName(), "fullName");
        ValidationUtil.validateEmail(request.getEmail());

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!user.getEmail().equalsIgnoreCase(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("email", "El correo electrónico ya está registrado");
        }

        UserRole role = parseRole(request.getRole());
        if (user.getId().equals(currentUserId) && user.getRole() != role) {
            throw new ValidationException("role", "No puedes cambiar tu propio rol");
        }

        user.setFullName(request.getFullName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setRole(role);

        return toResponseDTO(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponseDTO updateUserStatus(Long id, UserStatusUpdateRequest request, Long currentUserId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (user.getId().equals(currentUserId)) {
            throw new ValidationException("active", "No puedes cambiar el estado de tu propia cuenta");
        }

        user.setActive(request.getActive());
        return toResponseDTO(userRepository.save(user));
    }

    private UserResponseDTO toResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.isActive())
                .build();
    }

    private UserRole parseRole(String roleName) {
        try {
            return UserRole.valueOf(roleName.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("role", "Rol inválido");
        }
    }
}
