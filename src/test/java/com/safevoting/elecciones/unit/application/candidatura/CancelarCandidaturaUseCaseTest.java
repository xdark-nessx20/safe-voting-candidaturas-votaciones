package com.safevoting.elecciones.unit.application.candidatura;

import com.safevoting.elecciones.application.candidatura.CancelarCandidaturaUseCase;
import com.safevoting.elecciones.domain.exception.candidatura.CandidaturaYaCanceladaException;
import com.safevoting.elecciones.domain.model.candidatura.Candidatura;
import com.safevoting.elecciones.domain.model.candidatura.EstadoCandidatura;
import com.safevoting.elecciones.domain.model.votacion.EstadoVotacion;
import com.safevoting.elecciones.domain.model.votacion.Votacion;
import com.safevoting.elecciones.domain.repository.CandidaturaRepository;
import com.safevoting.elecciones.domain.repository.VotacionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelarCandidaturaUseCaseTest {

    @Mock
    private CandidaturaRepository candidaturaRepository;

    @Mock
    private VotacionRepository votacionRepository;

    @InjectMocks
    private CancelarCandidaturaUseCase useCase;

    private final UUID candidaturaId = UUID.randomUUID();
    private final UUID votacionId = UUID.randomUUID();

    @Test
    void cancelacionExitosa() {
        Candidatura candidatura = Candidatura.builder()
                .id(candidaturaId).miembroPartidoId(UUID.randomUUID())
                .partidoId(UUID.randomUUID()).votacionId(votacionId).build();
        Votacion votacion = Votacion.builder().id(votacionId).estado(EstadoVotacion.ACTIVA).build();

        when(candidaturaRepository.findById(candidaturaId)).thenReturn(Mono.just(candidatura));
        when(votacionRepository.findById(votacionId)).thenReturn(Mono.just(votacion));
        when(candidaturaRepository.update(any(Candidatura.class))).thenReturn(Mono.just(candidatura));

        StepVerifier.create(useCase.ejecutar(candidaturaId))
                .expectNextMatches(c -> !c.isActiva())
                .verifyComplete();
    }

    @Test
    void yaCanceladaLanzaCandidaturaYaCanceladaException() {
        Candidatura candidatura = Candidatura.builder()
                .id(candidaturaId).miembroPartidoId(UUID.randomUUID())
                .partidoId(UUID.randomUUID()).votacionId(votacionId)
                .estado(EstadoCandidatura.CANCELADA).build();
        Votacion votacion = Votacion.builder().id(votacionId).estado(EstadoVotacion.ACTIVA).build();

        when(candidaturaRepository.findById(candidaturaId)).thenReturn(Mono.just(candidatura));
        when(votacionRepository.findById(votacionId)).thenReturn(Mono.just(votacion));

        StepVerifier.create(useCase.ejecutar(candidaturaId))
                .expectError(CandidaturaYaCanceladaException.class)
                .verify();

        verify(candidaturaRepository, never()).update(any());
    }

    @Test
    void votacionExistentePermiteCancelacion() {
        Candidatura candidatura = Candidatura.builder()
                .id(candidaturaId).miembroPartidoId(UUID.randomUUID())
                .partidoId(UUID.randomUUID()).votacionId(votacionId).build();
        Votacion votacion = Votacion.builder().id(votacionId).estado(EstadoVotacion.EN_PROGRESO).build();

        when(candidaturaRepository.findById(candidaturaId)).thenReturn(Mono.just(candidatura));
        when(votacionRepository.findById(votacionId)).thenReturn(Mono.just(votacion));
        when(candidaturaRepository.update(any(Candidatura.class))).thenReturn(Mono.just(candidatura));

        StepVerifier.create(useCase.ejecutar(candidaturaId))
                .expectNextMatches(c -> !c.isActiva())
                .verifyComplete();
    }
}
