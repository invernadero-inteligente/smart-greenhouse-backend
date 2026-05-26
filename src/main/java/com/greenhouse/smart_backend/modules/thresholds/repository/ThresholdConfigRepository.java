package com.greenhouse.smart_backend.modules.thresholds.repository;

import com.greenhouse.smart_backend.modules.thresholds.model.ThresholdConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThresholdConfigRepository extends JpaRepository<ThresholdConfig, Long> {
    List<ThresholdConfig> findByZoneIdIn(List<Long> zoneIds);
    List<ThresholdConfig> findByZoneIdInAndVariableNameIn(List<Long> zoneIds, List<String> variableNames);
    boolean existsByZoneIdAndVariableName(Long zoneId, String variableName);
    Optional<ThresholdConfig> findByZoneIdAndVariableName(Long zoneId, String variableName);
}

