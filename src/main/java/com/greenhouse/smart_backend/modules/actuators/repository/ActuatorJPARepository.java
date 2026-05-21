package com.greenhouse.smart_backend.modules.actuators.repository;

import com.greenhouse.smart_backend.modules.actuators.model.Actuator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActuatorJPARepository extends JpaRepository<Actuator, Long> {
    Optional<Actuator> findByZoneIdAndName(Long zoneId, String name);
}
