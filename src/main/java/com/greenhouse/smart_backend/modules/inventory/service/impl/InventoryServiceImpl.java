package com.greenhouse.smart_backend.modules.inventory.service.impl;

import com.greenhouse.smart_backend.modules.inventory.dto.request.InventoryCreateRequest;
import com.greenhouse.smart_backend.modules.inventory.dto.request.InventoryUpdateRequest;
import com.greenhouse.smart_backend.modules.inventory.dto.response.InventoryListResponseDTO;
import com.greenhouse.smart_backend.modules.inventory.dto.response.InventoryResponseDTO;
import com.greenhouse.smart_backend.modules.inventory.model.InventoryCategory;
import com.greenhouse.smart_backend.modules.inventory.model.InventoryItem;
import com.greenhouse.smart_backend.modules.inventory.repository.InventoryRepository;
import com.greenhouse.smart_backend.modules.inventory.service.InventoryService;
import com.greenhouse.smart_backend.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<InventoryListResponseDTO> listItems(InventoryCategory category, Boolean lowStock) {
        List<InventoryItem> items;

        if (Boolean.TRUE.equals(lowStock) && category != null) {
            // Filtro combinado: categoría + stock bajo
            items = inventoryRepository.findAllByCategory(category).stream()
                    .filter(i -> i.getQuantity().compareTo(i.getMinStock()) <= 0)
                    .toList();
        } else if (Boolean.TRUE.equals(lowStock)) {
            items = inventoryRepository.findLowStockItems();
        } else if (category != null) {
            items = inventoryRepository.findAllByCategory(category);
        } else {
            items = inventoryRepository.findAll();
        }

        return items.stream().map(this::toListDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponseDTO getItemById(Long id) {
        return toResponseDTO(findOrThrow(id));
    }

    @Override
    @Transactional
    public InventoryResponseDTO createItem(InventoryCreateRequest request) {
        InventoryItem item = InventoryItem.builder()
                .name(request.getName())
                .category(request.getCategory())
                .quantity(request.getQuantity())
                .unit(request.getUnit())
                .minStock(request.getMinStock() != null
                        ? request.getMinStock()
                        : BigDecimal.ZERO)
                .build();

        item = inventoryRepository.save(item);
        log.info("Item de inventario creado con id: {}", item.getId());
        return toResponseDTO(item);
    }

    @Override
    @Transactional
    public InventoryResponseDTO updateItem(Long id, InventoryUpdateRequest request) {
        InventoryItem item = findOrThrow(id);

        if (request.getName() != null)     item.setName(request.getName());
        if (request.getCategory() != null) item.setCategory(request.getCategory());
        if (request.getQuantity() != null) item.setQuantity(request.getQuantity());
        if (request.getUnit() != null)     item.setUnit(request.getUnit());
        if (request.getMinStock() != null) item.setMinStock(request.getMinStock());

        inventoryRepository.save(item);
        log.info("Item de inventario actualizado con id: {}", item.getId());
        return toResponseDTO(item);
    }

    @Override
    @Transactional
    public void deleteItem(Long id) {
        InventoryItem item = findOrThrow(id);
        inventoryRepository.delete(item);
        log.info("Item de inventario eliminado con id: {}", id);
    }

    // ── Métodos privados ──────────────────────────────────────────────────────

    private InventoryItem findOrThrow(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Item de inventario no encontrado con id: " + id));
    }

    private boolean isLowStock(InventoryItem item) {
        return item.getQuantity().compareTo(item.getMinStock()) <= 0;
    }

    private InventoryListResponseDTO toListDTO(InventoryItem item) {
        return InventoryListResponseDTO.builder()
                .id(item.getId())
                .name(item.getName())
                .category(item.getCategory())
                .quantity(item.getQuantity())
                .unit(item.getUnit())
                .minStock(item.getMinStock())
                .lowStock(isLowStock(item))
                .build();
    }

    private InventoryResponseDTO toResponseDTO(InventoryItem item) {
        return InventoryResponseDTO.builder()
                .id(item.getId())
                .name(item.getName())
                .category(item.getCategory())
                .quantity(item.getQuantity())
                .unit(item.getUnit())
                .minStock(item.getMinStock())
                .lowStock(isLowStock(item))
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
