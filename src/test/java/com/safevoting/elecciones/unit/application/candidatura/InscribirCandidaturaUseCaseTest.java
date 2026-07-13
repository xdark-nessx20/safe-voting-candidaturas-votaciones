package com.safevoting.elecciones.unit.application.candidatura;

import com.safevoting.elecciones.application.candidatura.InscribirCandidaturaUseCase;
import com.safevoting.elecciones.domain.exception.candidatura.MiembroNoActivoException;
import com.safevoting.elecciones.domain.exception.candidatura.PartidoInhabilitadoException;
import com.safevoting.elecciones.domain.exception.candidatura.VotacionNoActivaException;
import com.safevoting.elecciones.domain.model.candidatura.Candidatura;
import com.safevoting.elecciones.domain.model.partido.EstadoMiembro;
import com.safevoting.elecciones.domain.model.partido.EstadoPartido;
import com.safevoting.elecciones.domain.model.partido.MiembroPartido;
import com.safevoting.elecciones.domain.model.partido.PartidoPolitico;
import com.safevoting.elecciones.domain.model.votacion.EstadoVotacion;
import com.safevoting.elecciones.domain.model.votacion.Votacion;
import com.safevoting.elecciones.domain.repository.CandidaturaRepository;
import com.safevoting.elecciones.domain.repository.MiembroPartidoRepository;
import com.safevoting.elecciones.domain.repository.PartidoPoliticoRepository;
import com.safevoting.elecciones.domain.repository.VotacionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InscribirCandidaturaUseCaseTest {

    @Mock
    private CandidaturaRepository candidaturaRepository;

    @Mock
    private VotacionRepository votacionRepository;

    @Mock
    private MiembroPartidoRepository miembroPartidoRepository;

    @Mock
    private PartidoPoliticoRepository partidoRepository;

    @InjectMocks
    private InscribirCandidaturaUseCase useCase;

    private final UUID miembroPartidoId = UUID.randomUUID();
    private final UUID votacionId = UUID.randomUUID();
    private final UUID partidoId = UUID.randomUUID();

    @Test
    void inscripcionExitosa() {
        Votacion votacion = Votacion.builder().id(votacionId).estado(EstadoVotacion.ACTIVA).build();
        MiembroPartido miembro = MiembroPartido.builder()
                .id(miembroPartidoId).partidoId(partidoId).estado(EstadoMiembro.ACTIVO)
                .nombreCompleto("Juan").documentoIdentidad("DNI").lugarInscripcion("Lima").build();
        PartidoPolitico partido = PartidoPolitico.builder().id(partidoId).estado(EstadoPartido.HABILITADO).build();

        when(votacionRepository.findById(votacionId)).thenReturn(Mono.just(votacion));
        when(miembroPartidoRepository.findById(miembroPartidoId)).thenReturn(Mono.just(miembro));
        when(partidoRepository.findById(partidoId)).thenReturn(Mono.just(partido));
        when(candidaturaRepository.save(any(Candidatura.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(useCase.ejecutar(miembroPartidoId, votacionId))
                .expectNextMatches(c -> c.isActiva() && c.getMiembroPartidoId().equals(miembroPartidoId))
                .verifyComplete();
    }

    @Test
    void votacionNoActivaLanzaVotacionNoActivaException() {
        Votacion votacion = Votacion.builder().id(votacionId).estado(EstadoVotacion.EN_PROGRESO).build();

        when(votacionRepository.findById(votacionId)).thenReturn(Mono.just(votacion));

        StepVerifier.create(useCase.ejecutar(miembroPartidoId, votacionId))
                .expectError(VotacionNoActivaException.class)
                .verify();

        verify(candidaturaRepository, never()).save(any());
    }

    @Test
    void miembroInactivoLanzaMiembroNoActivoException() {
        Votacion votacion = Votacion.builder().id(votacionId).estado(EstadoVotacion.ACTIVA).build();
        MiembroPartido miembro = MiembroPartido.builder()
                .id(miembroPartidoId).partidoId(partidoId).estado(EstadoMiembro.INACTIVO)
                .nombreCompleto("Juan").documentoIdentidad("DNI").lugarInscripcion("Lima").build();

        when(votacionRepository.findById(votacionId)).thenReturn(Mono.just(votacion));
        when(miembroPartidoRepository.findById(miembroPartidoId)).thenReturn(Mono.just(miembro));

        StepVerifier.create(useCase.ejecutar(miembroPartidoId, votacionId))
                .expectError(MiembroNoActivoException.class)
                .verify();

        verify(candidaturaRepository, never()).save(any());
    }

    @Test
    void partidoInhabilitadoLanzaPartidoInhabilitadoException() {
        Votacion votacion = Votacion.builder().id(votacionId).estado(EstadoVotacion.ACTIVA).build();
        MiembroPartido miembro = MiembroPartido.builder()
                .id(miembroPartidoId).partidoId(partidoId).estado(EstadoMiembro.ACTIVO)
                .nombreCompleto("Juan").documentoIdentidad("DNI").lugarInscripcion("Lima").build();
        PartidoPolitico partido = PartidoPolitico.builder().id(partidoId).estado(EstadoPartido.INHABILITADO).build();

        when(votacionRepository.findById(votacionId)).thenReturn(Mono.just(votacion));
        when(miembroPartidoRepository.findById(miembroPartidoId)).thenReturn(Mono.just(miembro));
        when(partidoRepository.findById(partidoId)).thenReturn(Mono.just(partido));

        StepVerifier.create(useCase.ejecutar(miembroPartidoId, votacionId))
                .expectError(PartidoInhabilitadoException.class)
                .verify();

        verify(candidaturaRepository, never()).save(any());
    }
}
