package com.greenhouse.smart_backend.modules.inventory.service;

import com.greenhouse.smart_backend.modules.inventory.dto.request.InventoryCreateRequest;
import com.greenhouse.smart_backend.modules.inventory.dto.request.InventoryUpdateRequest;
import com.greenhouse.smart_backend.modules.inventory.dto.response.InventoryListResponseDTO;
import com.greenhouse.smart_backend.modules.inventory.dto.response.InventoryResponseDTO;
import com.greenhouse.smart_backend.modules.inventory.model.InventoryCategory;

import java.util.List;

public interface InventoryService {
    List<InventoryListResponseDTO> listItems(InventoryCategory category, Boolean lowStock);
    InventoryResponseDTO getItemById(Long id);
    InventoryResponseDTO createItem(InventoryCreateRequest request);
    InventoryResponseDTO updateItem(Long id, InventoryUpdateRequest request);
    void deleteItem(Long id);
}
