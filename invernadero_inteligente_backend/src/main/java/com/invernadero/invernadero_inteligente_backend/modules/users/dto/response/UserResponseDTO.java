package com.invernadero.invernadero_inteligente_backend.modules.users.dto.response;

import com.invernadero.invernadero_inteligente_backend.modules.users.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String fullName;
    private String email;
    private UserRole role;
    private Boolean active;
}
