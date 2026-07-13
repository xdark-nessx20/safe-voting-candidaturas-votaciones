package com.safevoting.elecciones.unit.application.votacion;

import com.safevoting.elecciones.application.votacion.AbrirVotacionUseCase;
import com.safevoting.elecciones.domain.exception.votacion.SinCandidatosException;
import com.safevoting.elecciones.domain.exception.votacion.TransicionEstadoInvalidaException;
import com.safevoting.elecciones.domain.model.votacion.AlcanceVotacion;
import com.safevoting.elecciones.domain.model.votacion.EstadoVotacion;
import com.safevoting.elecciones.domain.model.votacion.TipoVotacion;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbrirVotacionUseCaseTest {

    @Mock
    private VotacionRepository repository;

    @Mock
    private CandidaturaRepository candidaturaRepository;

    @InjectMocks
    private AbrirVotacionUseCase useCase;

    private final UUID votacionId = UUID.randomUUID();

    @Test
    void aperturaExitosa() {
        Votacion votacion = Votacion.builder()
                .id(votacionId).nombre("Test").tipo(TipoVotacion.PRESIDENCIA)
                .alcance(AlcanceVotacion.NACIONAL).estado(EstadoVotacion.ACTIVA).build();

        when(repository.findById(votacionId)).thenReturn(Mono.just(votacion));
        when(candidaturaRepository.countActivasByVotacionId(votacionId)).thenReturn(Mono.just(3L));
        when(repository.update(any(Votacion.class))).thenReturn(Mono.just(votacion));

        StepVerifier.create(useCase.ejecutar(votacionId))
                .expectNextMatches(Votacion::isEnProgreso)
                .verifyComplete();
    }

    @Test
    void sinCandidatosLanzaSinCandidatosException() {
        Votacion votacion = Votacion.builder()
                .id(votacionId).nombre("Test").tipo(TipoVotacion.PRESIDENCIA)
                .alcance(AlcanceVotacion.NACIONAL).estado(EstadoVotacion.ACTIVA).build();

        when(repository.findById(votacionId)).thenReturn(Mono.just(votacion));
        when(candidaturaRepository.countActivasByVotacionId(votacionId)).thenReturn(Mono.just(0L));

        StepVerifier.create(useCase.ejecutar(votacionId))
                .expectError(SinCandidatosException.class)
                .verify();

        verify(repository, never()).update(any());
    }

    @Test
    void yaEnProgresoLanzaTransicionEstadoInvalidaException() {
        Votacion votacion = Votacion.builder()
                .id(votacionId).nombre("Test").tipo(TipoVotacion.PRESIDENCIA)
                .alcance(AlcanceVotacion.NACIONAL).estado(EstadoVotacion.EN_PROGRESO).build();

        when(repository.findById(votacionId)).thenReturn(Mono.just(votacion));
        when(candidaturaRepository.countActivasByVotacionId(votacionId)).thenReturn(Mono.just(3L));

        StepVerifier.create(useCase.ejecutar(votacionId))
                .expectError(TransicionEstadoInvalidaException.class)
                .verify();

        verify(repository, never()).update(any());
    }
}
