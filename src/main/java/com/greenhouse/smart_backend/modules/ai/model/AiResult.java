package com.greenhouse.smart_backend.modules.ai.model;

import com.greenhouse.smart_backend.modules.crops.model.Crop;
import com.greenhouse.smart_backend.modules.zones.model.Zone;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "ai_results")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_id")
    private Crop crop;

    @Enumerated(EnumType.STRING)
    @Column(name = "analysis_type", nullable = false, length = 50)
    private AIAnalysisType analysisType;

    @Column(name = "result_label", nullable = false, length = 100)
    private String resultLabel;

    /** Confianza del modelo entre 0.0000 y 1.0000 (ej: 0.9342 = 93.42%) */
    @Column(precision = 5, scale = 4)
    private BigDecimal confidence;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /** Datos adicionales del análisis en formato JSON almacenado como texto */
    @Column(columnDefinition = "jsonb")
    private String metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
