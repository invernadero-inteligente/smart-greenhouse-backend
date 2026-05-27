package com.greenhouse.smart_backend.modules.iot.mqtt.tasks;

import com.greenhouse.smart_backend.modules.iot.mqtt.application.MqttPublisherService;
import com.greenhouse.smart_backend.modules.thresholds.dto.response.ThresholdVariableResponseDTO;
import com.greenhouse.smart_backend.modules.thresholds.dto.response.ThresholdZoneResponseDTO;
import com.greenhouse.smart_backend.modules.thresholds.service.ThresholdService;
import com.greenhouse.smart_backend.modules.zones.model.Zone;
import com.greenhouse.smart_backend.modules.zones.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorRequestScheduler {

    private final MqttPublisherService mqttPublisherService;
    private final ZoneRepository zoneRepository;
    private final ThresholdService thresholdService;

    @Scheduled(fixedRate = 30000)
    public void requestSensorData() {
        List<Zone> activeZones = zoneRepository.findAllByIsActive(true);

        if (activeZones.isEmpty()) {
            log.info("No hay zonas activas para solicitar lecturas MQTT");
            return;
        }

        List<Long> zoneIds = activeZones.stream()
                .map(Zone::getId)
                .toList();

        Map<Long, Zone> zonesById = activeZones.stream()
                .collect(Collectors.toMap(Zone::getId, Function.identity()));

        List<ThresholdZoneResponseDTO> thresholdsByZone = thresholdService.listThresholds(zoneIds, null, null);

        if (thresholdsByZone.isEmpty()) {
            log.info("No hay thresholds configurados para las zonas activas");
            return;
        }

        for (ThresholdZoneResponseDTO thresholdZone : thresholdsByZone) {
            Zone zone = zonesById.get(thresholdZone.getZoneId());

            if (zone == null) {
                log.warn("No se encontró zona activa para zoneId={}", thresholdZone.getZoneId());
                continue;
            }

            String nodeName = zone.getName();

            for (ThresholdVariableResponseDTO variable : thresholdZone.getVariables()) {
                publishSensorRequest(nodeName, variable);
            }
        }
    }

    private void publishSensorRequest(String nodeName, ThresholdVariableResponseDTO variable) {
        try {
            String variableName = variable.getName();

            mqttPublisherService.publishSensorRequest(
                    nodeName,
                    variableName
            );

            log.info(
                    "Solicitud MQTT publicada. node={}, variable={}",
                    nodeName,
                    variableName
            );

        } catch (Exception e) {
            log.error(
                    "Error publicando solicitud MQTT para node={}",
                    nodeName,
                    e
            );
        }
    }
}