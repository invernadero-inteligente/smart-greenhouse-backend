package com.greenhouse.smart_backend.modules.alerts.repository;

import com.greenhouse.smart_backend.modules.alerts.model.Alert;
import com.greenhouse.smart_backend.modules.alerts.model.AlertStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AlertSpecification {

    private AlertSpecification() {}

    public static Specification<Alert> withFilters(
            AlertStatus status,
            Long zoneId,
            Long cropId,
            LocalDateTime from,
            LocalDateTime to) {

        return (root, query, cb) -> {
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
        };
    }
}
