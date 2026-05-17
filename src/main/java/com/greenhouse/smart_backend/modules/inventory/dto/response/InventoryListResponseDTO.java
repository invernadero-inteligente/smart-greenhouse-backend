package com.greenhouse.smart_backend.modules.inventory.dto.response;

import com.greenhouse.smart_backend.modules.inventory.model.InventoryCategory;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class InventoryListResponseDTO {
    private Long id;
    private String name;
    private InventoryCategory category;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal minStock;
    private boolean lowStock;
}
