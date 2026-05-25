package com.greenhouse.smart_backend.modules.inventory.dto.request;

import com.greenhouse.smart_backend.modules.inventory.model.InventoryCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InventoryCreateRequest {

    @NotBlank(message = "El nombre del item es obligatorio")
    private String name;

    @NotNull(message = "La categoría es obligatoria")
    private InventoryCategory category;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.0", message = "La cantidad no puede ser negativa")
    private BigDecimal quantity;

    @NotBlank(message = "La unidad es obligatoria")
    private String unit;

    @DecimalMin(value = "0.0", message = "El stock mínimo no puede ser negativo")
    private BigDecimal minStock;
}
