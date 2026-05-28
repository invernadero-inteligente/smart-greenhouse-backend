package com.greenhouse.smart_backend.modules.ai.repository;

import com.greenhouse.smart_backend.modules.ai.model.AiResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiResultRepository extends JpaRepository<AiResult, Long> {
}
