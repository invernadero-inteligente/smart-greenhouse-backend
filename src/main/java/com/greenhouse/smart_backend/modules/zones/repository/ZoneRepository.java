package com.greenhouse.smart_backend.modules.zones.repository;

import com.greenhouse.smart_backend.modules.zones.model.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long> {
    List<Zone> findAllByIsActiveTrue();
    List<Zone> findAllByIsActive(boolean isActive);
    boolean existsByName(String name);
    Optional<Zone> findByName(String name);
}