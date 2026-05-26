package com.greenhouse.smart_backend.modules.alerts.repository;

import com.greenhouse.smart_backend.modules.alerts.model.Alert;
import com.greenhouse.smart_backend.modules.alerts.model.AlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


@Repository
public interface AlertRepository extends JpaRepository<Alert, Long>,
        JpaSpecificationExecutor<Alert> {

    List<Alert> findAllByStatus(AlertStatus status);
    List<Alert> findAllByZoneId(Long zoneId);
    List<Alert> findAllByZoneIdAndStatus(Long zoneId, AlertStatus status);
    List<Alert> findAllByCropId(Long cropId);
    List<Alert> findAllByCropIdAndStatus(Long cropId, AlertStatus status);

    @Query("SELECT a FROM Alert a WHERE a.createdAt BETWEEN :from AND :to")
    List<Alert> findAllByDateRange(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}