package com.greenhouse.smart_backend.modules.zones.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ZoneCreateRequest {

    @NotBlank(message = "El nombre de la zona es obligatorio")
    private String name;

    private String description;
}
