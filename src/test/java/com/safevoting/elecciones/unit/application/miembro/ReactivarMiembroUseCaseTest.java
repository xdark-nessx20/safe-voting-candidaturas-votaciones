package com.safevoting.elecciones.unit.application.miembro;

import com.safevoting.elecciones.application.miembro.ReactivarMiembroUseCase;
import com.safevoting.elecciones.application.miembro.event.UsuarioHabilitadoEvent;
import com.safevoting.elecciones.domain.model.partido.EstadoMiembro;
import com.safevoting.elecciones.domain.model.partido.MiembroPartido;
import com.safevoting.elecciones.domain.repository.EventLogRepository;
import com.safevoting.elecciones.domain.repository.MiembroPartidoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReactivarMiembroUseCaseTest {

    @Mock
    private MiembroPartidoRepository miembroRepository;

    @Mock
    private EventLogRepository eventLogRepository;

    @InjectMocks
    private ReactivarMiembroUseCase useCase;

    private final UUID usuarioId = UUID.randomUUID();
    private final UUID miembroId = UUID.randomUUID();

    @Test
    void usuarioHabilitadoReactivaMiembrosInactivosPorInhabilitacion() {
        UsuarioHabilitadoEvent event = new UsuarioHabilitadoEvent("evt-001", Instant.now(), usuarioId, "Juan", "DNI", "Lima");
        MiembroPartido miembro = MiembroPartido.builder()
                .id(miembroId).usuarioId(usuarioId).partidoId(UUID.randomUUID())
                .nombreCompleto("Juan").documentoIdentidad("DNI").lugarInscripcion("Lima")
                .estado(EstadoMiembro.INACTIVO).motivoBaja("USUARIO_INHABILITADO: Motivo original")
                .build();

        when(eventLogRepository.existsByEventId("evt-001")).thenReturn(Mono.just(false));
        when(miembroRepository.findByUsuarioId(usuarioId)).thenReturn(Flux.just(miembro));
        when(miembroRepository.update(any(MiembroPartido.class))).thenReturn(Mono.just(miembro));
        when(eventLogRepository.saveEventId("evt-001")).thenReturn(Mono.empty());

        useCase.ejecutar(event).block();

        verify(miembroRepository).update(any(MiembroPartido.class));
    }

    @Test
    void eventoDuplicadoNoProcesa() {
        UsuarioHabilitadoEvent event = new UsuarioHabilitadoEvent("evt-003", Instant.now(), usuarioId, "Juan", "DNI", "Lima");

        when(eventLogRepository.existsByEventId("evt-003")).thenReturn(Mono.just(true));

        useCase.ejecutar(event).block();

        verify(miembroRepository, never()).findByUsuarioId(any());
        verify(miembroRepository, never()).update(any());
    }

    @Test
    void usuarioHabilitadoSobreMiembroYaActivoNoOp() {
        UsuarioHabilitadoEvent event = new UsuarioHabilitadoEvent("evt-004", Instant.now(), usuarioId, "Juan", "DNI", "Lima");
        MiembroPartido miembro = MiembroPartido.builder()
                .id(miembroId).usuarioId(usuarioId).partidoId(UUID.randomUUID())
                .nombreCompleto("Juan").documentoIdentidad("DNI").lugarInscripcion("Lima")
                .estado(EstadoMiembro.ACTIVO)
                .build();

        when(eventLogRepository.existsByEventId("evt-004")).thenReturn(Mono.just(false));
        when(miembroRepository.findByUsuarioId(usuarioId)).thenReturn(Flux.just(miembro));
        when(eventLogRepository.saveEventId("evt-004")).thenReturn(Mono.empty());

        useCase.ejecutar(event).block();

        verify(miembroRepository, never()).update(any());
    }
}
