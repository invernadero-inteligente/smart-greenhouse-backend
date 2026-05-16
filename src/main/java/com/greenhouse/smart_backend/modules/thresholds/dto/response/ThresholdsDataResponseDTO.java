package com.greenhouse.smart_backend.modules.thresholds.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThresholdsDataResponseDTO<T> {
    private T data;
}

