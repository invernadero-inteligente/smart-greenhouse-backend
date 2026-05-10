package com.invernadero.invernadero_inteligente_backend.modules.thresholds.model;

import com.invernadero.invernadero_inteligente_backend.modules.users.model.User;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "threshold_change_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThresholdChangeHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "threshold_config_id", nullable = false)
    private ThresholdConfig thresholdConfig;

    @Column(precision = 10, scale = 2)
    private BigDecimal oldMinValue;

    @Column(precision = 10, scale = 2)
    private BigDecimal oldMaxValue;

    @Column(precision = 10, scale = 2)
    private BigDecimal newMinValue;

    @Column(precision = 10, scale = 2)
    private BigDecimal newMaxValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id")
    private User changedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime changedAt;
    
    @PrePersist
    protected void onCreate() {
        changedAt = LocalDateTime.now();
    }
}
