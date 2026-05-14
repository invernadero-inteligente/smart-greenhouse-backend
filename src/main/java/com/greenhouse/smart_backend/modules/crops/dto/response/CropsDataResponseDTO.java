package com.greenhouse.smart_backend.modules.crops.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CropsDataResponseDTO<T> {
    private T data;
}
