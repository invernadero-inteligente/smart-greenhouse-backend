package com.greenhouse.smart_backend.modules.alerts.model;

import com.greenhouse.smart_backend.modules.crops.model.Crop;
import com.greenhouse.smart_backend.modules.sensors.model.Sensor;
import com.greenhouse.smart_backend.modules.users.model.User;
import com.greenhouse.smart_backend.modules.zones.model.Zone;
import com.greenhouse.smart_backend.shared.enums.SensorVariable;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id")
    private Sensor sensor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_id")
    private Crop crop;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AlertOrigin origin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SensorVariable variable;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AlertSeverity severity;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    /** Valor del sensor que disparó la alerta */
    @Column(precision = 10, scale = 2)
    private BigDecimal value;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private AlertStatus status = AlertStatus.OPEN;

    /** Usuario que atendió la alerta. Nulo mientras esté OPEN. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attended_by")
    private User attendedBy;

    @Column(name = "attended_at")
    private LocalDateTime attendedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
