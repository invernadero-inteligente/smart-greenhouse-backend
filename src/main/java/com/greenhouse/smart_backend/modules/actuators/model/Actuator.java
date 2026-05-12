package com.greenhouse.smart_backend.modules.actuators.model;

import com.greenhouse.smart_backend.modules.zones.model.Zone;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "actuators",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_actuators_zone_name",
        columnNames = {"zone_id", "name"}
    )
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Actuator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "current_action", nullable = false, length = 10)
    @Builder.Default
    private String currentAction = "OFF";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
