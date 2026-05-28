package com.greenhouse.smart_backend.modules.alerts.service;

import com.greenhouse.smart_backend.modules.alerts.dto.request.AlertCreateRequest;
import com.greenhouse.smart_backend.modules.alerts.dto.response.AlertResponseDTO;
import com.greenhouse.smart_backend.modules.alerts.model.Alert;
import com.greenhouse.smart_backend.modules.alerts.model.AlertOrigin;
import com.greenhouse.smart_backend.modules.alerts.model.AlertSeverity;
import com.greenhouse.smart_backend.modules.alerts.model.AlertStatus;
import com.greenhouse.smart_backend.modules.alerts.repository.AlertRepository;
import com.greenhouse.smart_backend.modules.crops.model.Crop;
import com.greenhouse.smart_backend.modules.crops.model.CropStatus;
import com.greenhouse.smart_backend.modules.crops.repository.CropRepository;
import com.greenhouse.smart_backend.modules.iot.document.SensorReadingDocument;
import com.greenhouse.smart_backend.modules.thresholds.model.ThresholdConfig;
import com.greenhouse.smart_backend.modules.thresholds.repository.ThresholdConfigRepository;
import com.greenhouse.smart_backend.modules.zones.model.Zone;
import com.greenhouse.smart_backend.modules.zones.repository.ZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutomaticAlertServiceTest {

    @Mock
    private AlertService alertService;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private CropRepository cropRepository;

    @Mock
    private ThresholdConfigRepository thresholdConfigRepository;

    @Mock
    private ZoneRepository zoneRepository;

    private AutomaticAlertService automaticAlertService;

    @BeforeEach
    void setUp() {
        automaticAlertService = new AutomaticAlertService(
                alertService,
                alertRepository,
                cropRepository,
                thresholdConfigRepository,
                zoneRepository);
    }

    @Test
    void shouldCreateCriticalAlertForOutOfRangeReading() {
        Zone zone = zone(1L, "Zona A");
        Crop crop = crop(9L, zone);
        ThresholdConfig threshold = threshold(zone, "TEMPERATURE", "C", 18, 30);
        SensorReadingDocument reading = reading("Zona A", "TEMPERATURE", "35", "C");

        when(zoneRepository.findByName("Zona A")).thenReturn(Optional.of(zone));
        when(thresholdConfigRepository.findByZoneIdAndVariableName(1L, "TEMPERATURE")).thenReturn(Optional.of(threshold));
        when(alertRepository.findAllByZoneIdAndVariableNameAndSeverityAndStatusAndCreatedAtGreaterThanEqual(
            eq(1L),
            eq("TEMPERATURE"),
            eq(AlertSeverity.HIGH),
            eq(AlertStatus.OPEN),
            any(LocalDateTime.class))).thenReturn(List.of());
        when(cropRepository.findAllByZoneIdAndStatus(1L, CropStatus.ACTIVE)).thenReturn(List.of(crop));
        when(alertService.createAlert(any(AlertCreateRequest.class))).thenReturn(alertResponse(55L));

        automaticAlertService.evaluateAndCreateAlert(reading);

        ArgumentCaptor<AlertCreateRequest> captor = ArgumentCaptor.forClass(AlertCreateRequest.class);
        verify(alertService).createAlert(captor.capture());

        AlertCreateRequest request = captor.getValue();
        assertThat(request.getZoneId()).isEqualTo(1L);
        assertThat(request.getCropId()).isEqualTo(9L);
        assertThat(request.getOrigin()).isEqualTo(AlertOrigin.IOT);
        assertThat(request.getVariableName()).isEqualTo("TEMPERATURE");
        assertThat(request.getUnit()).isEqualTo("C");
        assertThat(request.getSeverity()).isEqualTo(AlertSeverity.HIGH);
        assertThat(request.getValue()).isEqualByComparingTo(BigDecimal.valueOf(35));
        assertThat(request.getMessage()).contains("critical detectada");
    }

    @Test
    void shouldSkipDuplicateRecentAlerts() {
        Zone zone = zone(1L, "Zona A");
        ThresholdConfig threshold = threshold(zone, "TEMPERATURE", "C", 18, 30);
        SensorReadingDocument reading = reading("Zona A", "TEMPERATURE", "35", "C");

        when(zoneRepository.findByName("Zona A")).thenReturn(Optional.of(zone));
        when(thresholdConfigRepository.findByZoneIdAndVariableName(1L, "TEMPERATURE")).thenReturn(Optional.of(threshold));
        when(alertRepository.findAllByZoneIdAndVariableNameAndSeverityAndStatusAndCreatedAtGreaterThanEqual(
            eq(1L),
            eq("TEMPERATURE"),
            eq(AlertSeverity.HIGH),
            eq(AlertStatus.OPEN),
            any(LocalDateTime.class))).thenReturn(List.of(alert(1L, zone)));

        automaticAlertService.evaluateAndCreateAlert(reading);

        verify(alertService, never()).createAlert(any());
    }

    private Zone zone(Long id, String name) {
        return Zone.builder()
                .id(id)
                .name(name)
                .description("Zona de prueba")
                .isActive(true)
                .build();
    }

    private Crop crop(Long id, Zone zone) {
        return Crop.builder()
                .id(id)
                .zone(zone)
                .name("Tomate")
                .status(CropStatus.ACTIVE)
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

    private SensorReadingDocument reading(String nodeName, String variableName, String value, String unit) {
        return SensorReadingDocument.builder()
                .nodeName(nodeName)
                .variableName(variableName)
                .value(value)
                .unit(unit)
                .timestamp(Instant.now())
                .build();
    }

    private Alert alert(Long id, Zone zone) {
        return Alert.builder()
                .id(id)
                .zone(zone)
                .origin(AlertOrigin.IOT)
                .variableName("TEMPERATURE")
                .unit("C")
                .severity(AlertSeverity.HIGH)
                .status(AlertStatus.OPEN)
                .message("Alerta previa")
                .value(BigDecimal.valueOf(35))
                .createdAt(LocalDateTime.now())
                .build();
    }

    private AlertResponseDTO alertResponse(Long id) {
        return AlertResponseDTO.builder()
                .id(id)
                .build();
    }
}