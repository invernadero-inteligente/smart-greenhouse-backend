package com.greenhouse.smart_backend.modules.crops.model;

import com.greenhouse.smart_backend.shared.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_id", nullable = false)
    private Crop crop;

    @Column(nullable = false)
    private LocalDateTime conditionDate;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
