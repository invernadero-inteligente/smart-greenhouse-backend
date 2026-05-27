package com.greenhouse.smart_backend.modules.alerts.repository;

import com.greenhouse.smart_backend.modules.alerts.model.Alert;
import com.greenhouse.smart_backend.modules.alerts.model.AlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.criteria.Predicate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long>, JpaSpecificationExecutor<Alert> {

    List<Alert> findAllByStatus(AlertStatus status);

    List<Alert> findAllByZoneId(Long zoneId);

    List<Alert> findAllByZoneIdAndStatus(Long zoneId, AlertStatus status);

    List<Alert> findAllByCropId(Long cropId);

    List<Alert> findAllByCropIdAndStatus(Long cropId, AlertStatus status);

    @Query("SELECT a FROM Alert a WHERE a.createdAt BETWEEN :from AND :to")
    List<Alert> findAllByDateRange(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

        default List<Alert> findWithFilters(
                        AlertStatus status,
                        Long zoneId,
                        Long cropId,
                        LocalDateTime from,
                        LocalDateTime to) {
                return findAll((root, query, cb) -> {
                        List<Predicate> predicates = new ArrayList<>();

                        if (status != null) {
                                predicates.add(cb.equal(root.get("status"), status));
                        }
                        if (zoneId != null) {
                                predicates.add(cb.equal(root.get("zone").get("id"), zoneId));
                        }
                        if (cropId != null) {
                                predicates.add(cb.equal(root.get("crop").get("id"), cropId));
                        }
                        if (from != null) {
                                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
                        }
                        if (to != null) {
                                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
                        }

                        return cb.and(predicates.toArray(new Predicate[0]));
                });
        }
}
