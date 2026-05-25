package com.greenhouse.smart_backend.modules.dashboard.service.impl;

import com.greenhouse.smart_backend.modules.dashboard.dto.response.DashboardLatestReadingsResponseDTO;
import com.greenhouse.smart_backend.modules.iot.document.SensorReadingDocument;
import com.greenhouse.smart_backend.modules.iot.repository.SensorReadingMongoRepository;
import com.greenhouse.smart_backend.modules.thresholds.model.ThresholdConfig;
import com.greenhouse.smart_backend.modules.thresholds.repository.ThresholdConfigRepository;
import com.greenhouse.smart_backend.modules.zones.model.Zone;
import com.greenhouse.smart_backend.modules.zones.repository.ZoneRepository;
import com.greenhouse.smart_backend.shared.enums.ReadingStatus;
import com.greenhouse.smart_backend.shared.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private ZoneRepository zoneRepository;

    @Mock
    private ThresholdConfigRepository thresholdConfigRepository;

    @Mock
    private SensorReadingMongoRepository sensorReadingMongoRepository;

    private DashboardServiceImpl dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardServiceImpl(zoneRepository, thresholdConfigRepository, sensorReadingMongoRepository);
    }

    @Test
    void shouldConsolidateLatestReadingsForActiveZones() {
        Zone zone = zone(1L, "Zona A", true);
        Instant now = Instant.now();

        ThresholdConfig temperatureThreshold = threshold(zone, "TEMPERATURE", "C", 18, 30);
        ThresholdConfig humidityThreshold = threshold(zone, "AIR_HUMIDITY", "%", 40, 70);

        when(zoneRepository.findAllByIsActiveTrue()).thenReturn(List.of(zone));
        when(thresholdConfigRepository.findByZoneIdIn(List.of(1L))).thenReturn(List.of(temperatureThreshold, humidityThreshold));
        when(sensorReadingMongoRepository.findByNodeNameOrderByTimestampDesc("Zona A")).thenReturn(List.of(
                reading("Zona A", "TEMPERATURE", "20", "C", now.minusSeconds(120)),
                reading("Zona A", "AIR_HUMIDITY", "50", "%", now.minusSeconds(45)),
                reading("Zona A", "TEMPERATURE", "18", "C", now.minusSeconds(30)),
                reading("Zona A", "WATER_LEVEL", "abc", "cm", now.minusSeconds(20))
        ));

        DashboardLatestReadingsResponseDTO response = dashboardService.getLatestReadings(null);

        assertThat(response.getGeneratedAt()).isNotNull();
        assertThat(response.getZones()).hasSize(1);

        var zoneDto = response.getZones().get(0);
        assertThat(zoneDto.getZoneId()).isEqualTo(1L);
        assertThat(zoneDto.getZoneName()).isEqualTo("Zona A");
        assertThat(zoneDto.isOnline()).isTrue();
        assertThat(zoneDto.getLastReadingAt()).isEqualTo(now.minusSeconds(20));
        assertThat(zoneDto.getReadings()).hasSize(3);

        assertThat(zoneDto.getReadings())
                .extracting("variable")
                .containsExactly("AIR_HUMIDITY", "TEMPERATURE", "WATER_LEVEL");

        assertThat(zoneDto.getReadings().get(1).getStatus()).isEqualTo(ReadingStatus.WARNING);
        assertThat(zoneDto.getReadings().get(0).getStatus()).isEqualTo(ReadingStatus.NORMAL);
        assertThat(zoneDto.getReadings().get(2).getStatus()).isEqualTo(ReadingStatus.UNKNOWN);
        assertThat(zoneDto.getReadings().get(0).isOnline()).isTrue();
    }

    @Test
    void shouldReturnOnlyRequestedActiveZoneAndRejectInactiveZone() {
        Zone activeZone = zone(1L, "Zona A", true);
        Zone inactiveZone = zone(2L, "Zona B", false);

        when(zoneRepository.findById(1L)).thenReturn(Optional.of(activeZone));
        when(zoneRepository.findById(2L)).thenReturn(Optional.of(inactiveZone));
        when(thresholdConfigRepository.findByZoneIdIn(List.of(1L))).thenReturn(List.of());
        when(sensorReadingMongoRepository.findByNodeNameOrderByTimestampDesc("Zona A")).thenReturn(List.of());

        DashboardLatestReadingsResponseDTO response = dashboardService.getLatestReadings(1L);

        assertThat(response.getZones()).hasSize(1);
        assertThat(response.getZones().get(0).getZoneId()).isEqualTo(1L);
        assertThat(response.getZones().get(0).isActive()).isTrue();

        assertThatThrownBy(() -> dashboardService.getLatestReadings(2L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Zona no encontrada o inactiva");
    }

    private Zone zone(Long id, String name, boolean active) {
        return Zone.builder()
                .id(id)
                .name(name)
                .description("Descripción de prueba")
                .isActive(active)
                .build();
    }

    private ThresholdConfig threshold(Zone zone, String variableName, String unit, int minValue, int maxValue) {
        return ThresholdConfig.builder()
                .zone(zone)
                .variableName(variableName)
                .unit(unit)
                .minValue(BigDecimal.valueOf(minValue))
                .maxValue(BigDecimal.valueOf(maxValue))
                .build();
    }

    private SensorReadingDocument reading(String nodeName, String variableName, String value, String unit, Instant timestamp) {
        return SensorReadingDocument.builder()
                .nodeName(nodeName)
                .variableName(variableName)
                .value(value)
                .unit(unit)
                .timestamp(timestamp)
                .build();
    }
}

