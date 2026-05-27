package com.greenhouse.smart_backend.modules.ai.model;

import com.greenhouse.smart_backend.modules.zones.model.Zone;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "ai_results")
public class AiResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image", columnDefinition = "TEXT")
    private String image;
}