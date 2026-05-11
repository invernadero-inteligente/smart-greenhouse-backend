package com.greenhouse.smart_backend.modules.crops.model;

import com.greenhouse.smart_backend.shared.persistence.AuditableEntity;
import com.greenhouse.smart_backend.modules.zones.model.Zone;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
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

    @Column(length = 100)
    private String variety;

    @Column(name = "plant_count")
    private Integer plantCount;

    @Column(name = "sowing_date")
    private LocalDate sowingDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CropStatus status;
}
