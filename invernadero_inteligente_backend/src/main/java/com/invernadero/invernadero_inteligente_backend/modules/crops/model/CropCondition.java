package com.invernadero.invernadero_inteligente_backend.modules.crops.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "crop_conditions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CropCondition extends com.invernadero.invernadero_inteligente_backend.shared.persistence.AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_id", nullable = false)
    private Crop crop;

    @Column(nullable = false)
    private LocalDateTime conditionDate;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
