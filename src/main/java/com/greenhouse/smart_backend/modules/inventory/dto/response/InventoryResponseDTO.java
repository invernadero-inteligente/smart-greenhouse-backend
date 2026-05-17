package com.greenhouse.smart_backend.modules.inventory.dto.response;

import com.greenhouse.smart_backend.modules.inventory.model.InventoryCategory;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class InventoryResponseDTO {
    private Long id;
    private String name;
    private InventoryCategory category;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal minStock;
    private boolean lowStock;
    private LocalDateTime updatedAt;
}
