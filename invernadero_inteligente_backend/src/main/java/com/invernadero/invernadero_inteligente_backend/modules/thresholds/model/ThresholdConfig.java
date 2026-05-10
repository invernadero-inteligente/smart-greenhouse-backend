package com.invernadero.invernadero_inteligente_backend.modules.thresholds.model;

import com.invernadero.invernadero_inteligente_backend.shared.enums.SensorVariable;
import com.invernadero.invernadero_inteligente_backend.modules.users.model.User;
import com.invernadero.invernadero_inteligente_backend.modules.zones.model.Zone;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "threshold_configs")
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
    @Column(nullable = false)
    private SensorVariable sensorVariable;

    @Column(precision = 10, scale = 2)
    private BigDecimal minValue;

    @Column(precision = 10, scale = 2)
    private BigDecimal maxValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
