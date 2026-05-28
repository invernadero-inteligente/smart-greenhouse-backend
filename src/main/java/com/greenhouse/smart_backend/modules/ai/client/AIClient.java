package com.greenhouse.smart_backend.modules.ai.client;

import com.greenhouse.smart_backend.modules.ai.dto.response.AIAnalysisResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "ai-client", url = "${ai.service.url}")
public interface AIClient {
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    AIAnalysisResponseDTO analyzeImage(
            @RequestPart(value = "image")MultipartFile image
    );
}
