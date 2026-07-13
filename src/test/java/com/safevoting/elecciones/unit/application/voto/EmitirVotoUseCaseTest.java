package com.safevoting.elecciones.unit.application.voto;

import com.safevoting.elecciones.application.voto.EmitirVotoUseCase;
import com.safevoting.elecciones.domain.exception.participacion.CandidaturaNoActivaException;
import com.safevoting.elecciones.domain.exception.participacion.UsuarioYaVotoException;
import com.safevoting.elecciones.domain.exception.voto.VotacionNoEnProgresoException;
import com.safevoting.elecciones.domain.model.candidatura.Candidatura;
import com.safevoting.elecciones.domain.model.candidatura.EstadoCandidatura;
import com.safevoting.elecciones.domain.model.votacion.EstadoVotacion;
import com.safevoting.elecciones.domain.model.votacion.Votacion;
import com.safevoting.elecciones.domain.model.voto.Voto;
import com.safevoting.elecciones.domain.model.participacion.Participacion;
import com.safevoting.elecciones.domain.repository.CandidaturaRepository;
import com.safevoting.elecciones.domain.repository.ParticipacionRepository;
import com.safevoting.elecciones.domain.repository.VotacionRepository;
import com.safevoting.elecciones.domain.repository.VotoRepository;
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
class EmitirVotoUseCaseTest {

    @Mock
    private VotoRepository votoRepository;

    @Mock
    private ParticipacionRepository participacionRepository;

    @Mock
    private VotacionRepository votacionRepository;

    @Mock
    private CandidaturaRepository candidaturaRepository;

    @InjectMocks
    private EmitirVotoUseCase useCase;

    private final UUID usuarioId = UUID.randomUUID();
    private final UUID votacionId = UUID.randomUUID();
    private final UUID candidaturaId = UUID.randomUUID();
    private final UUID votoId = UUID.randomUUID();

    @Test
    void emisionExitosa() {
        Votacion votacion = Votacion.builder().id(votacionId).estado(EstadoVotacion.EN_PROGRESO).build();
        Candidatura candidatura = Candidatura.builder()
                .id(candidaturaId).miembroPartidoId(UUID.randomUUID())
                .partidoId(UUID.randomUUID()).votacionId(votacionId).build();
        Voto voto = Voto.builder().id(votoId).votacionId(votacionId).candidaturaId(candidaturaId).build();

        when(votacionRepository.findById(votacionId)).thenReturn(Mono.just(votacion));
        when(candidaturaRepository.findByVotacionIdAndCandidaturaId(votacionId, candidaturaId)).thenReturn(Mono.just(candidatura));
        when(participacionRepository.existsByUsuarioIdAndVotacionId(usuarioId, votacionId)).thenReturn(Mono.just(false));
        when(votoRepository.save(any(Voto.class))).thenReturn(Mono.just(voto));
        when(participacionRepository.save(any(Participacion.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(useCase.ejecutar(usuarioId, votacionId, candidaturaId))
                .expectNextMatches(p -> p.isValid() && p.getUsuarioId().equals(usuarioId))
                .verifyComplete();
    }

    @Test
    void votacionNoEnProgresoLanzaVotacionNoEnProgresoException() {
        Votacion votacion = Votacion.builder().id(votacionId).estado(EstadoVotacion.ACTIVA).build();

        when(votacionRepository.findById(votacionId)).thenReturn(Mono.just(votacion));

        StepVerifier.create(useCase.ejecutar(usuarioId, votacionId, candidaturaId))
                .expectError(VotacionNoEnProgresoException.class)
                .verify();

        verify(votoRepository, never()).save(any());
    }

    @Test
    void candidaturaNoActivaLanzaCandidaturaNoActivaException() {
        Votacion votacion = Votacion.builder().id(votacionId).estado(EstadoVotacion.EN_PROGRESO).build();
        Candidatura candidatura = Candidatura.builder()
                .id(candidaturaId).miembroPartidoId(UUID.randomUUID())
                .partidoId(UUID.randomUUID()).votacionId(votacionId)
                .estado(EstadoCandidatura.CANCELADA).build();

        when(votacionRepository.findById(votacionId)).thenReturn(Mono.just(votacion));
        when(candidaturaRepository.findByVotacionIdAndCandidaturaId(votacionId, candidaturaId)).thenReturn(Mono.just(candidatura));

        StepVerifier.create(useCase.ejecutar(usuarioId, votacionId, candidaturaId))
                .expectError(CandidaturaNoActivaException.class)
                .verify();

        verify(votoRepository, never()).save(any());
    }

    @Test
    void usuarioYaVotoLanzaUsuarioYaVotoException() {
        Votacion votacion = Votacion.builder().id(votacionId).estado(EstadoVotacion.EN_PROGRESO).build();
        Candidatura candidatura = Candidatura.builder()
                .id(candidaturaId).miembroPartidoId(UUID.randomUUID())
                .partidoId(UUID.randomUUID()).votacionId(votacionId).build();

        when(votacionRepository.findById(votacionId)).thenReturn(Mono.just(votacion));
        when(candidaturaRepository.findByVotacionIdAndCandidaturaId(votacionId, candidaturaId)).thenReturn(Mono.just(candidatura));
        when(participacionRepository.existsByUsuarioIdAndVotacionId(usuarioId, votacionId)).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.ejecutar(usuarioId, votacionId, candidaturaId))
                .expectError(UsuarioYaVotoException.class)
                .verify();

        verify(votoRepository, never()).save(any());
    }
}
