package com.greenhouse.smart_backend.modules.inventory.dto.request;

import com.greenhouse.smart_backend.modules.inventory.model.InventoryCategory;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InventoryUpdateRequest {
    private String name;
    private InventoryCategory category;

    @DecimalMin(value = "0.0", message = "La cantidad no puede ser negativa")
    private BigDecimal quantity;

    private String unit;

    @DecimalMin(value = "0.0", message = "El stock mínimo no puede ser negativo")
    private BigDecimal minStock;
}
