package com.greenhouse.smart_backend.modules.crops.model;

import com.greenhouse.smart_backend.shared.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "crop_conditions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CropCondition extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Relación 1:1 con Crop. Un cultivo tiene exactamente una configuración de condiciones ideales. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_id", nullable = false, unique = true)
    private Crop crop;

    @Column(name = "temperature_min", precision = 5, scale = 2)
    private BigDecimal temperatureMin;

    @Column(name = "temperature_max", precision = 5, scale = 2)
    private BigDecimal temperatureMax;

    @Column(name = "air_humidity_min", precision = 5, scale = 2)
    private BigDecimal airHumidityMin;

    @Column(name = "air_humidity_max", precision = 5, scale = 2)
    private BigDecimal airHumidityMax;

    @Column(name = "soil_moisture_min", precision = 5, scale = 2)
    private BigDecimal soilMoistureMin;

    @Column(name = "soil_moisture_max", precision = 5, scale = 2)
    private BigDecimal soilMoistureMax;

    @Column(name = "ph_min", precision = 4, scale = 2)
    private BigDecimal phMin;

    @Column(name = "ph_max", precision = 4, scale = 2)
    private BigDecimal phMax;
}
