package com.greenhouse.smart_backend.modules.thresholds.model;

import com.greenhouse.smart_backend.shared.enums.SensorVariable;
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
        name = "uq_threshold_zone_variable",
        columnNames = {"zone_id", "variable"}
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SensorVariable variable;

    @Column(name = "min_value", precision = 10, scale = 2)
    private BigDecimal minValue;

    @Column(name = "max_value", precision = 10, scale = 2)
    private BigDecimal maxValue;

    /** Usuario que realizó el último cambio en este umbral */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
