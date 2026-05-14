package com.greenhouse.smart_backend.modules.crops.repository;

import com.greenhouse.smart_backend.modules.crops.model.CropCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CropConditionRepository extends JpaRepository<CropCondition, Long> {
    Optional<CropCondition> findByCropId(Long cropId);
    void deleteByCropId(Long cropId);
}