package com.greenhouse.smart_backend.modules.actuators.service.impl;

import com.greenhouse.smart_backend.modules.actuators.dto.request.ActuatorCommandRequest;
import com.greenhouse.smart_backend.modules.actuators.dto.request.ActuatorCreateRequest;
import com.greenhouse.smart_backend.modules.actuators.dto.request.ActuatorUpdateRequest;
import com.greenhouse.smart_backend.modules.actuators.model.Actuator;
import com.greenhouse.smart_backend.modules.actuators.repository.ActuatorJPARepository;
import com.greenhouse.smart_backend.modules.actuators.service.ActuatorManagementService;
import com.greenhouse.smart_backend.modules.zones.model.Zone;
import com.greenhouse.smart_backend.modules.zones.repository.ZoneRepository;
import com.greenhouse.smart_backend.shared.enums.ActuatorAction;
import com.greenhouse.smart_backend.shared.exceptions.ResourceNotFoundException;
import com.greenhouse.smart_backend.shared.exceptions.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActuatorManagementServiceImplTest {

    @Mock
    private ActuatorJPARepository actuatorJPARepository;

    @Mock
    private ZoneRepository zoneRepository;

    @Mock
    private com.greenhouse.smart_backend.modules.iot.service.ActuatorService actuatorService;

    private ActuatorManagementService actuatorManagementService;

    @BeforeEach
    void setUp() {
        actuatorManagementService = new ActuatorManagementServiceImpl(
                actuatorJPARepository,
                zoneRepository,
                actuatorService);
    }

    @Test
    void shouldCreateActuatorWithDefaultOff() {
        Zone zone = zone(1L, "Zona 1");
        ActuatorCreateRequest request = new ActuatorCreateRequest();
        request.setZoneId(1L);
        request.setName("BOMBA");

        when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
        when(actuatorJPARepository.findByZoneIdAndName(1L, "BOMBA")).thenReturn(Optional.empty());
        when(actuatorJPARepository.save(any(Actuator.class))).thenAnswer(invocation -> {
            Actuator actuator = invocation.getArgument(0);
            actuator.setId(10L);
            actuator.setCreatedAt(LocalDateTime.now());
            return actuator;
        });

        var response = actuatorManagementService.createActuator(request);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getZoneId()).isEqualTo(1L);
        assertThat(response.getZoneName()).isEqualTo("Zona 1");
        assertThat(response.getName()).isEqualTo("BOMBA");
        assertThat(response.getCurrentAction()).isEqualTo(ActuatorAction.OFF);
        verify(actuatorJPARepository).save(any(Actuator.class));
    }

    @Test
    void shouldUpdateActuatorAndRejectDuplicates() {
        Zone zone = zone(1L, "Zona 1");
        Actuator actuator = actuator(5L, zone, "BOMBA", ActuatorAction.OFF.name());
        ActuatorUpdateRequest request = new ActuatorUpdateRequest();
        request.setName("VALVULA");
        request.setCurrentAction(ActuatorAction.ON);

        when(actuatorJPARepository.findById(5L)).thenReturn(Optional.of(actuator));
        when(actuatorJPARepository.findByZoneIdAndName(1L, "VALVULA")).thenReturn(Optional.empty());
        when(actuatorJPARepository.save(any(Actuator.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = actuatorManagementService.updateActuator(5L, request);

        assertThat(response.getName()).isEqualTo("VALVULA");
        assertThat(response.getCurrentAction()).isEqualTo(ActuatorAction.ON);
    }

    @Test
    void shouldExecuteCommandAndPublishToIotService() {
        Zone zone = zone(1L, "Zona 1");
        Actuator actuator = actuator(5L, zone, "BOMBA", ActuatorAction.OFF.name());
        Actuator updated = actuator(5L, zone, "BOMBA", ActuatorAction.ON.name());
        ActuatorCommandRequest request = new ActuatorCommandRequest();
        request.setAction(ActuatorAction.ON);

        when(actuatorJPARepository.findById(5L)).thenReturn(Optional.of(actuator)).thenReturn(Optional.of(updated));
        doNothing().when(actuatorService).saveActuatorPublisher(1L, "BOMBA", "ON");

        var response = actuatorManagementService.executeCommand(5L, request);

        assertThat(response.getCurrentAction()).isEqualTo(ActuatorAction.ON);
        verify(actuatorService).saveActuatorPublisher(1L, "BOMBA", "ON");
    }

    @Test
    void shouldListActuatorsByZone() {
        Zone zone = zone(1L, "Zona 1");
        Actuator actuator = actuator(5L, zone, "BOMBA", ActuatorAction.OFF.name());
        LocalDateTime updatedAt = LocalDateTime.now();
        actuator.setUpdatedAt(updatedAt);

        when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
        when(actuatorJPARepository.findAll()).thenReturn(List.of(actuator));

        var list = actuatorManagementService.listActuators(1L);

        assertThat(list).hasSize(1);
        assertThat(list.getFirst().getZoneName()).isEqualTo("Zona 1");
        assertThat(list.getFirst().getCurrentAction()).isEqualTo(ActuatorAction.OFF);
        assertThat(list.getFirst().getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void shouldDeleteActuator() {
        Zone zone = zone(1L, "Zona 1");
        Actuator actuator = actuator(5L, zone, "BOMBA", ActuatorAction.OFF.name());

        when(actuatorJPARepository.findById(5L)).thenReturn(Optional.of(actuator));

        actuatorManagementService.deleteActuator(5L);

        verify(actuatorJPARepository).delete(actuator);
    }

    @Test
    void shouldRejectDuplicateActuatorInSameZone() {
        Zone zone = zone(1L, "Zona 1");
        ActuatorCreateRequest request = new ActuatorCreateRequest();
        request.setZoneId(1L);
        request.setName("BOMBA");

        when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
        when(actuatorJPARepository.findByZoneIdAndName(1L, "BOMBA")).thenReturn(Optional.of(actuator(99L, zone, "BOMBA", ActuatorAction.OFF.name())));

        assertThatThrownBy(() -> actuatorManagementService.createActuator(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Ya existe un actuador con nombre: BOMBA");
    }

    @Test
    void shouldRejectMissingActuator() {
        when(actuatorJPARepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> actuatorManagementService.getActuatorById(5L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Actuador no encontrado");
    }

    private Zone zone(Long id, String name) {
        return Zone.builder()
                .id(id)
                .name(name)
                .description("Descripción de prueba")
                .isActive(true)
                .build();
    }

    private Actuator actuator(Long id, Zone zone, String name, String currentAction) {
        return Actuator.builder()
                .id(id)
                .zone(zone)
                .name(name)
                .currentAction(currentAction)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}


