package com.safevoting.elecciones.unit.application.miembro;

import com.safevoting.elecciones.application.miembro.ActualizarSnapshotUseCase;
import com.safevoting.elecciones.application.miembro.event.UsuarioActualizadoEvent;
import com.safevoting.elecciones.domain.model.candidatura.Candidatura;
import com.safevoting.elecciones.domain.model.partido.MiembroPartido;
import com.safevoting.elecciones.domain.model.votacion.EstadoVotacion;
import com.safevoting.elecciones.domain.model.votacion.Votacion;
import com.safevoting.elecciones.domain.repository.CandidaturaRepository;
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
class ActualizarSnapshotUseCaseTest {

    @Mock
    private MiembroPartidoRepository miembroRepository;

    @Mock
    private CandidaturaRepository candidaturaRepository;

    @Mock
    private EventLogRepository eventLogRepository;

    @InjectMocks
    private ActualizarSnapshotUseCase useCase;

    private final UUID usuarioId = UUID.randomUUID();
    private final UUID miembroId = UUID.randomUUID();

    @Test
    void miembroSinCandidaturasActivasSnapshotActualizado() {
        UsuarioActualizadoEvent event = new UsuarioActualizadoEvent("evt-001", Instant.now(), usuarioId, "Nuevo Nombre", "Nuevo DNI", "Nuevo Lugar");
        MiembroPartido miembro = MiembroPartido.builder()
                .id(miembroId).usuarioId(usuarioId).partidoId(UUID.randomUUID())
                .nombreCompleto("Viejo Nombre").documentoIdentidad("Viejo DNI").lugarInscripcion("Viejo Lugar")
                .build();

        when(eventLogRepository.existsByEventId("evt-001")).thenReturn(Mono.just(false));
        when(miembroRepository.findByUsuarioId(usuarioId)).thenReturn(Flux.just(miembro));
        when(candidaturaRepository.findActivasByMiembroId(miembroId)).thenReturn(Flux.empty());
        when(miembroRepository.update(any(MiembroPartido.class))).thenReturn(Mono.just(miembro));
        when(eventLogRepository.saveEventId("evt-001")).thenReturn(Mono.empty());

        useCase.ejecutar(event).block();

        verify(miembroRepository).update(any(MiembroPartido.class));
    }

    @Test
    void miembroConCandidaturaEnEnProgresoSnapshotNoActualizado() {
        UsuarioActualizadoEvent event = new UsuarioActualizadoEvent("evt-002", Instant.now(), usuarioId, "Nuevo Nombre", "Nuevo DNI", "Nuevo Lugar");
        MiembroPartido miembro = MiembroPartido.builder()
                .id(miembroId).usuarioId(usuarioId).partidoId(UUID.randomUUID())
                .nombreCompleto("Viejo Nombre").documentoIdentidad("Viejo DNI").lugarInscripcion("Viejo Lugar")
                .build();

        UUID candidaturaId = UUID.randomUUID();
        Candidatura candidatura = Candidatura.builder()
                .id(candidaturaId).miembroPartidoId(miembroId).partidoId(UUID.randomUUID()).votacionId(UUID.randomUUID()).build();
        Votacion votacion = Votacion.builder()
                .id(UUID.randomUUID()).nombre("Votacion 2026").estado(EstadoVotacion.EN_PROGRESO).build();

        when(eventLogRepository.existsByEventId("evt-002")).thenReturn(Mono.just(false));
        when(miembroRepository.findByUsuarioId(usuarioId)).thenReturn(Flux.just(miembro));
        when(candidaturaRepository.findActivasByMiembroId(miembroId)).thenReturn(Flux.just(candidatura));
        when(candidaturaRepository.findVotacionByCandidaturaId(candidaturaId)).thenReturn(Mono.just(votacion));
        when(eventLogRepository.saveEventId("evt-002")).thenReturn(Mono.empty());

        useCase.ejecutar(event).block();

        verify(miembroRepository, never()).update(any());
    }

    @Test
    void eventoDuplicadoNoProcesa() {
        UsuarioActualizadoEvent event = new UsuarioActualizadoEvent("evt-003", Instant.now(), usuarioId, "Nombre", "DNI", "Lugar");

        when(eventLogRepository.existsByEventId("evt-003")).thenReturn(Mono.just(true));

        useCase.ejecutar(event).block();

        verify(miembroRepository, never()).findByUsuarioId(any());
        verify(miembroRepository, never()).update(any());
    }
}
