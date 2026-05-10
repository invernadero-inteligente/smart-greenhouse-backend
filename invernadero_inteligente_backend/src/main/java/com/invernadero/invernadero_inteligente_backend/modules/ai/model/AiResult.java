package com.invernadero.invernadero_inteligente_backend.modules.ai.model;

import com.invernadero.invernadero_inteligente_backend.modules.crops.model.Crop;
import com.invernadero.invernadero_inteligente_backend.modules.zones.model.Zone;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AIAnalysisType analysisType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String resultData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_id")
    private Crop crop;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
