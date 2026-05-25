package com.greenhouse.smart_backend.modules.users.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserStatusUpdateRequest {

    @NotNull(message = "El estado activo es obligatorio")
    private Boolean active;
}
