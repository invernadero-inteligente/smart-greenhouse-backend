package com.greenhouse.smart_backend.modules.sensors.model;

import com.greenhouse.smart_backend.shared.persistence.AuditableEntity;
import com.greenhouse.smart_backend.shared.enums.SensorVariable;
import com.greenhouse.smart_backend.modules.zones.model.Zone;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sensors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sensor extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    /** Nombre descriptivo del sensor (ej: "Sensor Temperatura Zona A") */
    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SensorVariable variable;

    /** Unidad de medida (ej: "°C", "%", "pH") */
    @Column(length = 20)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SensorStatus status;
}
