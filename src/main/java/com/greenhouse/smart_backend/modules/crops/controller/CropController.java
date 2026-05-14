package com.greenhouse.smart_backend.modules.crops.controller;

import com.greenhouse.smart_backend.modules.crops.dto.request.CropCreateRequest;
import com.greenhouse.smart_backend.modules.crops.dto.request.CropUpdateRequest;
import com.greenhouse.smart_backend.modules.crops.dto.response.CropListResponseDTO;
import com.greenhouse.smart_backend.modules.crops.dto.response.CropResponseDTO;
import com.greenhouse.smart_backend.modules.crops.model.CropStatus;
import com.greenhouse.smart_backend.modules.crops.service.CropService;
import com.greenhouse.smart_backend.shared.responses.ApiResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/crops")
@RequiredArgsConstructor
@Slf4j
public class CropController {

    private final CropService cropService;

     /**
     * GET /api/crops
     * Lista todos los cultivos.
     * Accesible por todos los usuarios autenticados.
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<CropListResponseDTO>>> listCrops(
            @RequestParam(required = false) CropStatus status,
            @RequestParam(required = false) Long zoneId) {

        log.info("GET /api/crops - status={}, zoneId={}", status, zoneId);
        List<CropListResponseDTO> crops = cropService.listCrops(status, zoneId);
        return ResponseEntity.ok(ApiResponseDTO.success(crops));
    }

    /**
     * GET /api/crops/{id}
     * Retorna el detalle de un cultivo.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<CropResponseDTO>> getCropById(@PathVariable Long id) {
        log.info("GET /api/crops/{}", id);
        return ResponseEntity.ok(ApiResponseDTO.success(cropService.getCropById(id)));
    }

    /**
     * POST /api/crops
     * Crea un nuevo cultivo. Solo ADMIN.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<CropResponseDTO>> createCrop(
            @Valid @RequestBody CropCreateRequest request) {

        log.info("POST /api/crops - name={}", request.getName());
        CropResponseDTO created = cropService.createCrop(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success("Cultivo registrado exitosamente", created));
    }

    /**
     * PATCH /api/crops/{id}
     * Actualiza parcialmente un cultivo. Solo ADMIN.
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<CropResponseDTO>> updateCrop(
            @PathVariable Long id,
            @Valid @RequestBody CropUpdateRequest request) {

        log.info("PATCH /api/crops/{}", id);
        CropResponseDTO updated = cropService.updateCrop(id, request);
        return ResponseEntity.ok(ApiResponseDTO.success("Cultivo actualizado exitosamente", updated));
    }
}
