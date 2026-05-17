package com.greenhouse.smart_backend.modules.inventory.controller;

import com.greenhouse.smart_backend.modules.inventory.dto.request.InventoryCreateRequest;
import com.greenhouse.smart_backend.modules.inventory.dto.request.InventoryUpdateRequest;
import com.greenhouse.smart_backend.modules.inventory.dto.response.InventoryListResponseDTO;
import com.greenhouse.smart_backend.modules.inventory.dto.response.InventoryResponseDTO;
import com.greenhouse.smart_backend.modules.inventory.model.InventoryCategory;
import com.greenhouse.smart_backend.modules.inventory.service.InventoryService;
import com.greenhouse.smart_backend.shared.responses.ApiResponseDTO;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Inventory Controller", description = "Endpoints para gestión de inventario")
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirements({
        @SecurityRequirement(name = "bearerAuth")
})
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * GET /api/inventory?category=SEEDS&lowStock=true
     * Lista todos los items. Filtros opcionales por categoría y stock bajo.
     * Accesible por todos los usuarios autenticados.
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<InventoryListResponseDTO>>> listItems(
            @RequestParam(required = false) InventoryCategory category,
            @RequestParam(required = false) Boolean lowStock) {

        log.info("GET /api/inventory - category={}, lowStock={}", category, lowStock);
        return ResponseEntity.ok(
                ApiResponseDTO.success(inventoryService.listItems(category, lowStock)));
    }

    /**
     * GET /api/inventory/{id}
     * Retorna el detalle de un item.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<InventoryResponseDTO>> getItemById(
            @PathVariable Long id) {

        log.info("GET /api/inventory/{}", id);
        return ResponseEntity.ok(
                ApiResponseDTO.success(inventoryService.getItemById(id)));
    }

    /**
     * POST /api/inventory
     * Registra un nuevo item. Solo ADMIN y OPERATOR.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponseDTO<InventoryResponseDTO>> createItem(
            @Valid @RequestBody InventoryCreateRequest request) {

        log.info("POST /api/inventory - name={}", request.getName());
        InventoryResponseDTO created = inventoryService.createItem(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success("Item registrado exitosamente", created));
    }

    /**
     * PATCH /api/inventory/{id}
     * Actualiza parcialmente un item. Solo ADMIN y OPERATOR.
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponseDTO<InventoryResponseDTO>> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody InventoryUpdateRequest request) {

        log.info("PATCH /api/inventory/{}", id);
        InventoryResponseDTO updated = inventoryService.updateItem(id, request);
        return ResponseEntity.ok(
                ApiResponseDTO.success("Item actualizado exitosamente", updated));
    }

    /**
     * DELETE /api/inventory/{id}
     * Elimina un item del inventario. Solo ADMIN.
     * A diferencia de zonas y cultivos, los items de inventario sí se eliminan
     * físicamente porque no tienen dependencias con otros módulos.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> deleteItem(@PathVariable Long id) {

        log.info("DELETE /api/inventory/{}", id);
        inventoryService.deleteItem(id);
        return ResponseEntity.ok(
                ApiResponseDTO.success("Item eliminado exitosamente", null));
    }
}
