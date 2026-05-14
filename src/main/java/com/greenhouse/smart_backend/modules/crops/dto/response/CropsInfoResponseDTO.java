package com.greenhouse.smart_backend.modules.crops.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class CropsInfoResponseDTO {
    private Long id;
    private String name;
    private String variety;
    private Integer plantCount;
    private Date sowingDate;
    private String status;
}
