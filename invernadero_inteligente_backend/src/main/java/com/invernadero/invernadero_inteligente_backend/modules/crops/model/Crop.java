package com.invernadero.invernadero_inteligente_backend.modules.crops.model;

import com.invernadero.invernadero_inteligente_backend.shared.persistence.AuditableEntity;
import com.invernadero.invernadero_inteligente_backend.modules.zones.model.Zone;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "crops")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Crop extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "crop_type", nullable = false, length = 100)
    private String type;

    private LocalDateTime plantedAt;
    private LocalDateTime expectedHarvestAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CropStatus status;
}
