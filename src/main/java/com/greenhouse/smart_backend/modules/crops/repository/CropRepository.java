package com.greenhouse.smart_backend.modules.crops.repository;

import com.greenhouse.smart_backend.modules.crops.model.Crop;
import com.greenhouse.smart_backend.modules.crops.model.CropStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CropRepository extends JpaRepository<Crop, Long> {
    List<Crop> findAllByZoneId(Long zoneId);
    List<Crop> findAllByStatus(CropStatus status);
    List<Crop> findAllByZoneIdAndStatus(Long zoneId, CropStatus status);
}
