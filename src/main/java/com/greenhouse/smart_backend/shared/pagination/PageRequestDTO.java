package com.greenhouse.smart_backend.shared.pagination;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitudes de paginaciÃ³n
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequestDTO {

    private int page = 0;
    private int size = 20;
    private String sort = "id";
    private String direction = "ASC";

    public int getPage() {
        return Math.max(page, 0);
    }

    public int getSize() {
        if (size < 1)
            return 20;
        return Math.min(size, 100);
    }
}
