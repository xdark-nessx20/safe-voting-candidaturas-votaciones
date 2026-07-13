package com.safevoting.elecciones.unit.application.miembro;

import com.safevoting.elecciones.application.miembro.DarBajaMiembroUseCase;
import com.safevoting.elecciones.domain.exception.miembro.MiembroInscritoEnVotacionException;
import com.safevoting.elecciones.domain.exception.miembro.MiembroNoEncontradoException;
import com.safevoting.elecciones.domain.exception.miembro.MiembroYaInactivoException;
import com.safevoting.elecciones.domain.model.candidatura.Candidatura;
import com.safevoting.elecciones.domain.model.partido.EstadoMiembro;
import com.safevoting.elecciones.domain.model.partido.MiembroPartido;
import com.safevoting.elecciones.domain.model.votacion.EstadoVotacion;
import com.safevoting.elecciones.domain.model.votacion.Votacion;
import com.safevoting.elecciones.domain.repository.CandidaturaRepository;
import com.safevoting.elecciones.domain.repository.MiembroPartidoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DarBajaMiembroUseCaseTest {

    @Mock
    private MiembroPartidoRepository miembroRepository;

    @Mock
    private CandidaturaRepository candidaturaRepository;

    @InjectMocks
    private DarBajaMiembroUseCase useCase;

    private final UUID miembroId = UUID.randomUUID();

    @Test
    void bajaExitosaConMotivo() {
        MiembroPartido miembro = MiembroPartido.builder()
                .id(miembroId)
                .usuarioId(UUID.randomUUID())
                .partidoId(UUID.randomUUID())
                .nombreCompleto("Juan Perez")
                .documentoIdentidad("DNI-12345678")
                .lugarInscripcion("Lima")
                .estado(EstadoMiembro.ACTIVO)
                .build();

        when(miembroRepository.findById(miembroId)).thenReturn(Mono.just(miembro));
        when(candidaturaRepository.findActivasByMiembroId(miembroId)).thenReturn(Flux.empty());
        when(miembroRepository.update(any(MiembroPartido.class))).thenReturn(Mono.just(miembro));

        StepVerifier.create(useCase.ejecutar(miembroId, "Baja voluntaria"))
                .expectNextMatches(m -> m.isInactivo()
                        && "Baja voluntaria".equals(m.getMotivoBaja()))
                .verifyComplete();
    }

    @Test
    void miembroNoEncontradoLanzaMiembroNoEncontradoException() {
        when(miembroRepository.findById(miembroId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.ejecutar(miembroId, "Motivo"))
                .expectError(MiembroNoEncontradoException.class)
                .verify();

        verify(miembroRepository, never()).update(any());
    }

    @Test
    void miembroYaInactivoLanzaMiembroYaInactivoException() {
        MiembroPartido miembro = MiembroPartido.builder()
                .id(miembroId)
                .usuarioId(UUID.randomUUID())
                .partidoId(UUID.randomUUID())
                .nombreCompleto("Juan Perez")
                .documentoIdentidad("DNI-12345678")
                .lugarInscripcion("Lima")
                .estado(EstadoMiembro.INACTIVO)
                .build();

        when(miembroRepository.findById(miembroId)).thenReturn(Mono.just(miembro));
        when(candidaturaRepository.findActivasByMiembroId(miembroId)).thenReturn(Flux.empty());

        StepVerifier.create(useCase.ejecutar(miembroId, "Motivo"))
                .expectError(MiembroYaInactivoException.class)
                .verify();

        verify(miembroRepository, never()).update(any());
    }

    @Test
    void conCandidaturasEnVotacionEnProgresoLanzaMiembroInscritoEnVotacionException() {
        MiembroPartido miembro = MiembroPartido.builder()
                .id(miembroId)
                .usuarioId(UUID.randomUUID())
                .partidoId(UUID.randomUUID())
                .nombreCompleto("Juan Perez")
                .documentoIdentidad("DNI-12345678")
                .lugarInscripcion("Lima")
                .estado(EstadoMiembro.ACTIVO)
                .build();

        UUID candidaturaId = UUID.randomUUID();
        Candidatura candidatura = Candidatura.builder()
                .id(candidaturaId)
                .miembroPartidoId(miembroId)
                .partidoId(UUID.randomUUID())
                .votacionId(UUID.randomUUID())
                .build();
        Votacion votacion = Votacion.builder()
                .id(UUID.randomUUID())
                .nombre("Votacion 2026")
                .estado(EstadoVotacion.EN_PROGRESO)
                .build();

        when(miembroRepository.findById(miembroId)).thenReturn(Mono.just(miembro));
        when(candidaturaRepository.findActivasByMiembroId(miembroId)).thenReturn(Flux.just(candidatura));
        when(candidaturaRepository.findVotacionByCandidaturaId(candidaturaId)).thenReturn(Mono.just(votacion));

        StepVerifier.create(useCase.ejecutar(miembroId, "Motivo"))
                .expectError(MiembroInscritoEnVotacionException.class)
                .verify();

        verify(miembroRepository, never()).update(any());
    }
}
