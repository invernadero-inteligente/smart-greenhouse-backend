package com.invernadero.invernadero_inteligente_backend.modules.sensors.model;

import com.invernadero.invernadero_inteligente_backend.shared.persistence.AuditableEntity;
import com.invernadero.invernadero_inteligente_backend.shared.enums.SensorVariable;
import com.invernadero.invernadero_inteligente_backend.modules.zones.model.Zone;
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

    @Column(nullable = false, unique = true, length = 100)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SensorVariable variable;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SensorStatus status;
}
