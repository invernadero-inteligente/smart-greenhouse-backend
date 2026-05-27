package com.greenhouse.smart_backend.modules.thresholds.model;

import com.greenhouse.smart_backend.modules.users.model.User;
import com.greenhouse.smart_backend.modules.zones.model.Zone;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "threshold_configs",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_threshold_zone_variable_name",
        columnNames = {"zone_id", "variable_name"}
    )
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThresholdConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @Column(name = "variable_name", nullable = false, length = 100)
    private String variableName;

    @Column(name = "unit", nullable = false, length = 50)
    private String unit;

    @Column(name = "min_value", precision = 10, scale = 2, nullable = false)
    private BigDecimal minValue;

    @Column(name = "max_value", precision = 10, scale = 2, nullable = false)
    private BigDecimal maxValue;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void preSave() {
        this.updatedAt = LocalDateTime.now();
    }
}
